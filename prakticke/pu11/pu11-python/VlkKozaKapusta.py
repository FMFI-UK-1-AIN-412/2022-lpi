#!/usr/bin/env python3
import os
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '..', '..', '..', 'examples/sat')]
import sat

Vlavo = 'vlavo'
Vpravo = 'vpravo'
Breh = [ Vlavo, Vpravo ]
Str2Breh = { y:x for x,y in enumerate(Breh) }

Vlk = 'vlk'
Koza = 'koza'
Kapusta = 'kapusta'
Gazda = 'gazda'
Kto = [ Vlk, Koza, Kapusta, Gazda ]
Str2Kto = { y:x for x,y in enumerate(Kto) }

class VlkKozaKapusta(object):
    def vyries(self, pociatocnyStav, N):
        return []

if __name__ == '__main__':
    s = {
        Vlk: Vpravo,
        Koza: Vlavo,
        Kapusta: Vpravo,
        Gazda: Vlavo
    }
    print(VlkKozaKapusta().vyries(s,1))
