
sqaureTextures = [
  THREE.ImageUtils.loadTexture("/assets/images/square0.png"),
  THREE.ImageUtils.loadTexture("/assets/images/square1.png"),
  THREE.ImageUtils.loadTexture("/assets/images/square2.png"),
  THREE.ImageUtils.loadTexture("/assets/images/square3.png"),
]

class Square
  constructor: (@id, @size, @x, @y, @colorId) ->
    @sprite = new THREE.Sprite( { map: sqaureTextures[colorId], useScreenCoordinates: false, alignment: THREE.SpriteAlignment.center } );
    @sprite.position.set(@x, @y, 0 )
    @sprite.scale.set(1, 1, 1)
    @sprite.opacity = 0.7

class Squares
  constructor: (@scene) ->
    @squares = {}

  updateFromServer: (data) =>
    type = data.type
    id   = data.id

    if type == "UpdateSquare"
      if id not of @squares
        square = new Square(id, data.size, data.x, data.y, data.color)
        @squares[id] = square
        @scene.add(square.sprite)

      else
        @squares[id].sprite.scale.set(data.size, data.size, 1.0);

    if type == "RemoveSquare"
      @scene.remove(@squares[id].sprite)
      delete @squares[id]


