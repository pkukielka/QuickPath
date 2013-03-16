class Players
  constructor: (@allParticles, @defaultParticleSize, @scene) ->
    @players = {}

  update: ->
    for username, model of @players
      model.update()

  randColor: ->
    r = ('0'+(Math.random()*256|0).toString(16)).slice(-2)
    g = ('0'+(Math.random()*256|0).toString(16)).slice(-2)
    b = ('0'+(Math.random()*256|0).toString(16)).slice(-2)
    '0x' + r + g + b

  updateFromServer: (e) =>
    data     = JSON.parse(e.data)
    username = data.username
    position = new THREE.Vector3(data.x, data.y, data.z)

    if username not of @players
      @players[username] = new Trail(@allParticles, @defaultParticleSize, new THREE.Color(@randColor()))
      @scene.add(@players[username].particles)

    @players[username].moveTo(position)

    for name, model of @players
      if name != username
        model.collide(position)
