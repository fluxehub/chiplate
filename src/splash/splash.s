; designed for wernsey's chip-8 compiler
; https://github.com/wernsey/chip8

start:
    CLS

    ; load initial values
    ; v0, v1 - screen coords
    ; v2     - draw loop counter
    ; vd     - memory offset
    ld V0, #B
    ld V1, #B
    ld V2, #0
    ld VD, #9
    ld I, title0

titleloop:
    ; start loop
    drw V0, V1, #9
    
    ; inc x coord, memory and loop count
    add V0, #8
    add V2, #1
    add I, VD

    ; don't loop if title is fully drawn
    se V2, #6
    JP titleloop
    
    ; set up for round two
    ld V0, #C
    ld V1, #18
    ld V2, #0
    ld VD, #4

subtitleLoop:
    ; same stuff different day
    drw V0, V1, #4

    add V0, #8
    add V2, #1
    add I, VD

    ; don't loop if subtitle is fully drawn
    se V2, #5
    JP subtitleloop

loop:
    ; draw is done, loop forever
    JP loop

; technically data could all be expressed as one huge block
; but i break it up to clarify how it's drawn
title0: db
    %11111111,
    %10000000,
    %10011001,
    %10100101,
    %10100001,
    %10100101,
    %10011001,
    %10000000,
    %11111111

title1: db
    %11111111,
    %00000000,
    %00101011,
    %00101010,
    %11101011,
    %00101010,
    %00101010,
    %00000000,
    %11111111

title2: db
    %11111111,
    %00000000,
    %10010000,
    %01010000,
    %10010000,
    %00010000,
    %00011110,
    %00000000,
    %11111111

title3: db
    %11111111,
    %00000000,
    %01100111,
    %10010001,
    %11110001,
    %10010001,
    %10010001,
    %00000000,
    %11111111

title4: db
    %11111111,
    %00000000,
    %11011110,
    %00010000,
    %00011100,
    %00010000,
    %00011110,
    %00000000,
    %11111111

title5: db
    %10000000,
    %10000000,
    %10000000,
    %10000000,
    %10000000,
    %10000000,
    %10000000,
    %10000000,
    %10000000

subtitle0: db
    %11100111,
    %10010100,
    %10010111,
    %11100101

subtitle1: db
    %00111101,
    %10100101,
    %00100101,
    %00111101

subtitle2: db
    %11000011,
    %00100010,
    %11000011,
    %00000010

subtitle3: db
    %10011110,
    %01010010,
    %10010010,
    %10011110

subtitle4: db
    %11111010,
    %10101010,
    %10101000,
    %10001010
