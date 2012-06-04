function Unit(ownerId, id, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.ownerId = ownerId;
    this.id = id;
    this.j = j;
    this.i = i;
    this.sloader = new SpriteLoader(SPRITES);
    this.selected = false;
    //this.spriteOrientation = "S";
    this.spritePos = {
        x: Map.getCellXOnMap(this.j),
        y: Map.getCellYOnMap(this.i),
        arrived: true,
        orientation: "S"
    };
    
    //this.smoothMovement = new SmoothMovement(this.j, this.i, this.j, this.i, this.spriteOrientation);
    //this.smoothMovementWay = new Array();
    
    this.getOwnerId = function() {
        return this.ownerId;
    }
    this.getId = function() {
        return this.id;
    }
    this.getJ = function() {
        return this.j;
    }
    this.getI = function() {
        return this.i;
    }
    this.draw = function(context, camera) {
        var sprite = this.sloader.getSpriteByName("peasant_" + this.spritePos.orientation);
        var drawCoordinates;
        if (smoothMovementEnabled) {
            drawCoordinates = sprite.getDrawCoordinatesByXY(this.spritePos.x, this.spritePos.y, camera);
        } else {
            drawCoordinates = sprite.getDrawCoordinatesByXY(Map.getCellXOnMap(this.j), Map.getCellYOnMap(this.i), camera);
        }
        context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        if (this.selected) {
            sprite = this.sloader.getSpriteByName("peasant_selection");
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        }
        return;
        
        
        //var drawCoordinates;
        
        /*if (this.smoothMovementWay.length > 1) {
            var prevWayPoint;
            var curWayPoint;
            var diffArrivalTime;
            var curTime = (new Date()).getTime();
            var pastTime;
            var i;
            
            for (i = 0; i < this.smoothMovementWay.length - 1; i++) {
                prevWayPoint = this.smoothMovementWay[i];
                curWayPoint = this.smoothMovementWay[i + 1];
                diffArrivalTime = curWayPoint.arrivalTime - prevWayPoint.arrivalTime;
                if (curWayPoint.arrivalTime + diffArrivalTime > curTime) {
                    break;
                }
            }
            this.spriteOrientation = curWayPoint.movementOrientation;*/
            //this.smoothMovementWay = this.smoothMovementWay.slice(i);
            
            //prevWayPoint = this.smoothMovementWay[this.smoothMovementWay.length - 2];
            //curWayPoint = this.smoothMovementWay[this.smoothMovementWay.length - 1];
            /*pastTime = curTime - diffArrivalTime;
            var curPos = curWayPoint.getPosOnTheWayFrom(
                prevWayPoint.j,
                prevWayPoint.i,
                pastTime - prevWayPoint.arrivalTime
            );*/
            //console.log(curPos);
            /*console.log(
                Map.getCellXOnMap(prevWayPoint.j),
                ",",
                Map.getCellYOnMap(prevWayPoint.i),
                "->",
                Map.getCellXOnMap(curWayPoint.j),
                ",",
                Map.getCellYOnMap(curWayPoint.i),
                "pos",
                curPos.x,
                ",",
                curPos.y
            );*/
            /*sprite = this.sloader.getSpriteByName("peasant_" + curWayPoint.movementOrientation);
            drawCoordinates = sprite.getDrawCoordinatesByXY(curPos.x, curPos.y, camera);
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
            if (this.selected) {
                sprite = this.sloader.getSpriteByName("peasant_selection");
                context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
            }*/
            /*if (this.smoothMovementWay[this.smoothMovementWay.length - 1].isWayFinished(this.j, this.i)) {
                this.smoothMovementWay = new Array();
                console.log("Clear!");
            }*/
        /*    if (curWayPoint.isLast && Math.abs(Map.getCellXOnMap(this.j) - curPos.x) < 1.0 && Math.abs(Map.getCellYOnMap(this.i) - curPos.y) < 1.0) {
                this.smoothMovementWay = new Array();
                console.log("Clear!");
            }
            return;
        } else {
            sprite = this.sloader.getSpriteByName("peasant_" + this.spriteOrientation);
            drawCoordinates = sprite.getDrawCoordinates(this.j, this.i, camera);
            if (this.PrevPos != undefined
                    && (Math.abs(this.PrevPos.x - drawCoordinates.x) > 4.0
                            || Math.abs(this.PrevPos.y - drawCoordinates.y) > 4.0)) {

                console.log("derp2", this.PrevCell, {j: this.j, i: this.i});
            }
            this.PrevPos = drawCoordinates;
            var debugJ = this.j;
            var debugI = this.i;
            this.PrevCell = {j: debugJ, i: debugI};
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
            if (this.selected) {
                sprite = this.sloader.getSpriteByName("peasant_selection");
                context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
            }
            return;
        }*/
        
        /*/var tAxis = camera.translateAxis(Map.getCellXOnMap(this.j), Map.getCellYOnMap(this.i));
        var sprite;
        var newSpriteOrientation = movementManager.getNextPathPointOrientation(this.id);
        if (newSpriteOrientation != null && newSpriteOrientation != this.spriteOrientation) {
            this.spriteOrientation = newSpriteOrientation;
        }
        sprite = this.sloader.getSpriteByName("peasant_" + this.spriteOrientation);
        //drawCoordinates = sprite.getDrawCoordinates(this.j, this.i);
        //context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        var smooth = this.smoothMovement.getCurPos();
        drawCoordinates = sprite.getDrawCoordinatesByXY(smooth.x, smooth.y);
        context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        context.fillText("[" + drawCoordinates.x + "," + drawCoordinates.y + "]", drawCoordinates.x - 10, drawCoordinates.y - 10);
        if (this.selected) {
            sprite = this.sloader.getSpriteByName("peasant_selection");
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        }*/
    }
    
    return this;
}

//function PrintSmoothMovementWay(smoothMovementWay) {
//    var out = "";
//    for (var i = 0; i < smoothMovementWay.length; i++) {
//        out += JSON.stringify(smoothMovementWay[i]) + "\n";
//    }
//    return out;
//}
//
//function SmoothMovement(begJ, begI, endJ, endI, spriteOrientation) {
//    this.begX = Map.getCellXOnMap(begJ);
//    this.begY = Map.getCellYOnMap(begI);
//    this.curX = Map.getCellXOnMap(begJ);
//    this.curY = Map.getCellYOnMap(begI);
//    this.endX = Map.getCellXOnMap(endJ);
//    this.endY = Map.getCellYOnMap(endI);
//    this.spriteOrientation = spriteOrientation;
//    
//    this.tryRestart = function(newBegJ, newBegI, newEndJ, newEndI) {
//        if (this.begX != Map.getCellXOnMap(newBegJ) || this.begY != Map.getCellYOnMap(newBegI)
//                || this.endX != Map.getCellXOnMap(newEndJ) || this.endY != Map.getCellYOnMap(newEndI)) {
//            
//            this.begX = Map.getCellXOnMap(newBegJ);
//            this.begY = Map.getCellYOnMap(newBegI);
//            console.log("Jump", Math.abs(this.curX - this.begX), Math.abs(this.curY - this.begY), this.begX - this.curX, this.begY - this.curY);
//            this.curX = this.begX;
//            this.curY = this.begY;
//            this.endX = Map.getCellXOnMap(newEndJ);
//            this.endY = Map.getCellYOnMap(newEndI);
//            
//            var jDiff = newEndJ - newBegJ;
//            var iDiff = newEndI - newBegI;
//            if (jDiff == 0 && iDiff == -1) {
//                this.spriteOrientation = "N";
//            } else if (jDiff == 1 && iDiff == -1) {
//                this.spriteOrientation = "NE";
//            } else if (jDiff == 1 && iDiff == 0) {
//                this.spriteOrientation = "E";
//            } else if (jDiff == 1 && iDiff == 1) {
//                this.spriteOrientation = "SE";
//            } else if (jDiff == 0 && iDiff == 1) {
//                this.spriteOrientation = "S";
//            } else if (jDiff == -1 && iDiff == 1) {
//                this.spriteOrientation = "SW";
//            } else if (jDiff == -1 && iDiff == 0) {
//                this.spriteOrientation = "W";
//            } else {
//                this.spriteOrientation = "NW";
//            }
//        } else {
//            //console.log("Smooth movement not changes.[", newBegJ, newBegI, newEndJ, newEndI, "] [", this.begX, this.begY, this.endX, this.endY);
//        }
//    }
//    this.getCurPos = function() {
//        var inc = getPxIncrement(0.4);
//        switch (this.spriteOrientation) {
//            case "N":
//                if (this.curY > this.endY) {
//                    if (this.curY - inc < this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY -= inc;
//                    }
//                }
//                break;
//            case "NE":
//                if (this.curX < this.endX && this.curY > this.endY) {
//                    if (this.curX + inc > this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX += inc;
//                    }
//                    if (this.curY - inc < this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY -= inc;
//                    }
//                }
//                break;
//            case "E":
//                if (this.curX < this.endX) {
//                    if (this.curX + inc > this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX += inc;
//                    }
//                }
//                break;
//            case "SE":
//                if (this.curX < this.endX && this.curY < this.endY) {
//                    if (this.curX + inc > this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX += inc;
//                    }
//                    if (this.curY + inc > this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY += inc;
//                    }
//                }
//                break;
//            case "S":
//                if (this.curY < this.endY) {
//                    if (this.curY + inc > this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY += inc;
//                    }
//                }
//                break;
//            case "SW":
//                if (this.curX > this.endX && this.curY < this.endY) {
//                    if (this.curX - inc < this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX -= inc;
//                    }
//                    if (this.curY + inc > this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY += inc;
//                    }
//                }
//                break;
//            case "W":
//                if (this.curX > this.endX) {
//                    if (this.curX - inc < this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX -= inc;
//                    }
//                }
//                break;
//            case "NW":
//                if (this.curX > this.endX && this.curY > this.endY) {
//                    if (this.curX - inc < this.endX) {
//                        this.curX = this.endX;
//                    } else {
//                        this.curX -= inc;
//                    }
//                    if (this.curY - inc < this.endY) {
//                        this.curY = this.endY;
//                    } else {
//                        this.curY -= inc;
//                    }
//                }
//                break;
//            default:
//                console.log("Incorrect sprite orientation.");
//                break;
//        }
//        return {x: this.curX, y: this.curY};
//    }
//    
//    return this;
//}
//
//function SmoothMovementWayPoint(j, i, arrivalTime, debugPrevTime, arrivalSpeed, movementOrientation, isLast) {
//    this.j = j;
//    this.i = i;
//    this.arrivalTime = arrivalTime;
//    this.debugDiffTime = arrivalTime - debugPrevTime;
//    this.arrivalSpeed = arrivalSpeed;
//    this.movementOrientation = movementOrientation;
//    this.isLast = isLast;
//    this.getPosOnTheWayFrom = function(begJ, begI, diffTime) {
//        var begX = Map.getCellXOnMap(begJ);
//        var begY = Map.getCellYOnMap(begI);
//        var curX;
//        var curY;
//        var endX = Map.getCellXOnMap(this.j);
//        var endY = Map.getCellYOnMap(this.i);
//        var diffS = this.arrivalSpeed * diffTime;
//        
//        var fix = true;
//        
//        switch (this.movementOrientation) {
//            case "N":
//                curX = endX;
//                curY = begY - diffS;
//                if (fix && curY < endY) {
//                    curY = endY;
//                }
//                break;
//            case "NE":
//                curX = begX + diffS;
//                curY = begY - diffS;
//                if (fix && curX > endX) {
//                    curX = endX;
//                }
//                if (fix && curY < endY) {
//                    curY = endY;
//                }
//                break;
//            case "E":
//                curX = begX + diffS;
//                curY = endY;
//                if (fix && curX > endX) {
//                    curX = endX;
//                }
//                break;
//            case "SE":
//                curX = begX + diffS;
//                curY = begY + diffS;
//                if (fix && curX > endX) {
//                    curX = endX;
//                }
//                if (fix && curY > endY) {
//                    curY = endY;
//                }
//                break;
//            case "S":
//                curX = endX;
//                curY = begY + diffS;
//                if (fix && curY > endY) {
//                    curY = endY;
//                }
//                break;
//            case "SW":
//                curX = begX - diffS;
//                curY = begY + diffS;
//                if (fix && curX < endX) {
//                    curX = endX;
//                }
//                if (fix && curY > endY) {
//                    curY = endY;
//                }
//                break;
//            case "W":
//                curX = begX - diffS;
//                curY = endY;
//                if (fix && curX < endX) {
//                    curX = endX;
//                }
//                break;
//            case "NW":
//                curX = begX - diffS;
//                curY = begY - diffS;
//                if (fix && curX < endX) {
//                    curX = endX;
//                }
//                if (fix && curY < endY) {
//                    curY = endY;
//                }
//                break;
//            default:
//                console.log("Incorrect movement orientation.");
//                break;
//        }
//        
//        return {x: curX, y: curY};
//    }
//    this.isWayFinished = function(j, i) {
//        return this.isLast && this.j == j && this.i == i;
//    }
//    
//    return this;
//}
