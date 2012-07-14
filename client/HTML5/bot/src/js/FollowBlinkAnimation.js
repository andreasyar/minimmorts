function FollowBlinkAnimation(unit) {
    this.unit = unit;
    
    this.sloader = new SpriteLoader(SPRITES);
    
    this.begTime = (new Date()).getTime();
    
    this.draw = function(context, camera) {
        var curTime = (new Date()).getTime();
        
        if (this.begTime + 3000 < curTime) {
            var i = 0;
            if ( (i = freeAnimation.indexOf(this)) != -1) {
                freeAnimation.splice(i, 1);
                return;
            }
        }
        
        var diff = curTime - this.begTime;
        if (Math.ceil(diff / 500) % 2 == 0) {
            var sprite = this.sloader.getSpriteByName("peasant_follow");
            var drawCoordinates = sprite.getDrawCoordinatesByXY(this.unit.spritePos.x, this.unit.spritePos.y, camera);
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        }
    }
    
    return this;
}
