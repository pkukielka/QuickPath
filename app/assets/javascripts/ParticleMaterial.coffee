class ParticleMaterial
  constructor: ->
    @texture = new THREE.Texture(@generateSpriteTexture())
    @texture.needsUpdate = true;

  generate: (color) ->
    new THREE.ShaderMaterial
      uniforms:
        color:      {type: "c", value: color}
        texture:    {type: "t", value: @texture}
      attributes:
        alpha:      {type: 'f', value: []}
        size:       {type: 'f', value: []}
      vertexShader:   document.getElementById('vertexshader').textContent
      fragmentShader: document.getElementById('fragmentshader').textContent
      transparent: true

  generateSpriteTexture: ->
    canvas = document.createElement('canvas')
    canvas.width = 128;
    canvas.height = 128;

    context = canvas.getContext('2d')
    context.beginPath()
    context.arc( 64, 64, 60, 0, Math.PI * 2, false)
    context.closePath()

    gradient = context.createRadialGradient(canvas.width / 2, canvas.height / 2, 0, canvas.width / 2, canvas.height / 2, canvas.width / 2)
    gradient.addColorStop(0, 'rgba(255,255,255,0.9)')
    gradient.addColorStop(0.2, 'rgba(255,255,255,0.6)')
    gradient.addColorStop(0.4, 'rgba(200,200,200,0.8)')
    gradient.addColorStop(1, 'rgba(100,100,100,0)')

    context.fillStyle = gradient
    context.fill()

    return canvas