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
    r + g + b

  updateFromServer: (data) =>
    username = data.username

    if data.type == "Move"
      position = new THREE.Vector3(data.x, data.y, data.z)
      if username not of @players
        color = @randColor()
        @players[username] = new Trail(@allParticles, @defaultParticleSize, new THREE.Color('0x' + color))
        @players[username].color = color
        @players[username].score = 0
        @scene.add(@players[username].particles)

      @players[username].moveTo(position)


    if data.type == "Collision"
      position = new THREE.Vector3(data.x, data.y, data.z)
      @players[username].collide(position)

    if data.type == "UpdateScore"
      @players[username].score = data.score

      score.innerHTML = ""
      for username, model of @players
        score.innerHTML += "<div style='background-color: #" +  model.color + "'>Score: " +  model.score + "</div>"