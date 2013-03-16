class Scene
  constructor: ->
    if !Detector.webgl then Detector.addGetWebGLMessage()

    @scene = new THREE.Scene()
    @scene.fog = new THREE.FogExp2(0x000000, 0.0005)

    @renderer = new THREE.WebGLRenderer(clearColor: 0x66CCFF, clearAlpha: 1, antialias: false)
    @renderer.setSize(window.innerWidth, window.innerHeight)

    @camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 1, 10000)
    @camera.position.z = 100

    @stats = new Stats()
    @stats.domElement.style.position = 'absolute'
    @stats.domElement.style.top      = '5px'
    @stats.domElement.style.left     = '5px'

    @particleSize = 4
    @trail = new Trail(50, 4, new THREE.Color(0xFF1493))
    @scene.add(@trail.particles)

    pointerLock = new PointerLock(
      () => blocker.style.display  = 'none'
      () => blocker.style.display  = 'block'
      () => instructions.innerHTML = 'Your browser doesn\'t seem to support Pointer Lock API'
    )

    @ws = new Websocket((event) =>
      msg = JSON.parse(event.data)
      pos = new THREE.Vector3(msg.x, msg.y, msg.z)
      @trail.moveTo(pos)
      @trail.collide(pos)
    )

    pointerLock.onMouseMove(@particleSize, @particleSize * 1.5, (position) => @ws.send(position))

    instructions.addEventListener('click', (event) => pointerLock.lockPointer())

    document.body.appendChild(@renderer.domElement)
    document.body.appendChild(@stats.domElement)

  animate: =>
    requestAnimationFrame(@animate)
    @renderer.render(@scene, @camera)
    @stats.update()
    @trail.update()