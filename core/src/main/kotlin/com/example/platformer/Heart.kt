package com.example.platformer

data class Heart(
    var x: Float,
    var y: Float,
    var stateTime: Float = 0f  // for animation, if you want to animate hearts later
)
