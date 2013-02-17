class Scene
  constructor: ->
    if !Detector.webgl then Detector.addGetWebGLMessage()

    @isEnabled = false

    @scene = new THREE.Scene()
    @scene.fog = new THREE.FogExp2(0x000000, 0.0005)

    @renderer = new THREE.WebGLRenderer(clearColor: 0x66CCFF, clearAlpha: 1, antialias: false)
    @renderer.setSize(window.innerWidth, window.innerHeight)

    @camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 1, 10000)

    @stats = new Stats()
    @stats.domElement.style.position = 'absolute'
    @stats.domElement.style.top = '5px'
    @stats.domElement.style.left = '5px'

    pointerLock = new PointerLock(
      () => blocker.style.display = 'none';  @isEnabled = true
      () => blocker.style.display = 'block'; @isEnabled = false
      () => instructions.innerHTML = 'Your browser doesn\'t seem to support Pointer Lock API'
    )
    instructions.addEventListener('click', (event) => pointerLock.lockPointer())

    document.body.appendChild(@renderer.domElement)
    document.body.appendChild(@stats.domElement)

  animate: =>
    requestAnimationFrame(@animate)
    @renderer.render(@scene, @camera)
    @stats.update()
