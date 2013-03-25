class Websocket
  constructor: (@onMessage) ->
    @isConnected       = false
    @reconnectInterval = 1000

    @connect()

  connect: ->
    try
      @socket = new WebSocket(jsRoutes.controllers.Application.stream().webSocketURL())

      @socket.onmessage =
        @onMessage

      @socket.onerror = (err) ->
        console.error(err)

      @socket.onopen = () =>
        @isConnected = true

      @socket.onclose = ->
        @isConnected = false
        console.error("WebSocket connection is down. reconnecting...")
        setTimeout(@connect, @reconnectInterval)

    catch err
      console.error(err)

  send: (object) =>
    @socket.send(JSON.stringify(object)) if @isConnected