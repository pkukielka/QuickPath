class Trail
  constructor: (@allParticles, @defaultParticleSize, color) ->
    @geometry   = new THREE.Geometry
    @material   = new ParticleMaterial().generate(color)
    @vertices   = @geometry.vertices               = (new THREE.Vector3(0, 0, 0) for [1..@allParticles])
    @alpha      = @material.attributes.alpha.value = (0.0                        for [1..@allParticles])
    @size       = @material.attributes.size.value  = (@defaultParticleSize       for [1..@allParticles])

    @moveTo(new THREE.Vector3(0, 0, 0))
    @particles  = new THREE.ParticleSystem(@geometry, @material)

  pushFront: (array, elem) ->
    array.pop()
    array.unshift(elem)

  moveTo: (position) ->
    @pushFront(@vertices, position)
    @pushFront(@alpha,    1.0)
    @pushFront(@size,     @defaultParticleSize)

    for i in [20..@allParticles]
      @alpha[i] -= 0.02
      @size[i]  -= @defaultParticleSize * 0.015

    @geometry.verticesNeedUpdate = true

  update: ->
    @material.attributes.size.needsUpdate  = true
    @material.attributes.alpha.needsUpdate = true

    for i in [1...@allParticles]
      if @vertices[i].isExploding? and @alpha[i] > 0.0
        @alpha[i] -= 0.05
        @size[i]  *= 1.05
        if @vertices[i - 1].shouldExplode? then @vertices[i - 1].mark = true
        if (i + 1) != @allParticles        then @vertices[i + 1].mark = true

    for i in [1...@allParticles]
      if @vertices[i].mark? then @vertices[i].isExploding = true

  collide: (startPoint) ->
    for i in [0...@allParticles]
      if startPoint.distanceTo(@vertices[i]) <= @defaultParticleSize and @alpha[i] > 0.0
        @vertices[i].isExploding = true
        for j in [1...@allParticles]
          @vertices[j].shouldExplode = true