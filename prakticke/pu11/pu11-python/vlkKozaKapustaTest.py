#!/usr/bin/env python3
#
# Testovaci program pre planovanie

import sys
import time
import traceback
import copy

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

from VlkKozaKapusta import VlkKozaKapusta

""" Nastavte na True ak chcete aby testy zastali na prvej chybe. """
stopOnError = False

class FailedTestException(BaseException):
    pass

def now():
    try:
       return time.perf_counter() # >=3.3
    except AttributeError:
       return time.time() # this is not monotonic!

def printException():
    print('ERROR: Exception raised:\n%s\n%s\n%s' % (
        '-'*20,
        traceback.format_exc(),
        '-'*20)
    )

class Tester(object):
    def __init__(self):
        self.case = 0
        self.tested = 0
        self.passed = 0

    def fail(self, msg):
        print('ERROR: %s' % msg)
        return False;

    def checkGood(self, s0, n, r):
        if n != len(r):
            return self.fail('  Wrong result length!')

        k = 0
        s = copy.copy(s0)
        print('  %2d: %-7s %s' % (k, "", s))
        for a in r:
            k += 1
            if a not in Kto:
                return self.fail('Wrong action: %s' % a)
            if s[a] != s[Gazda]:
                return self.fail('%s is on other side then %s' % (a,Gazda))
            s[a] = Breh[ 1 - Str2Breh[s[a]] ]
            s[Gazda] = s[a]
            print('  %2d: %-7s %s' % (k, a, s))

            for x,y in ( (Vlk, Koza), (Koza, Kapusta)):
                if s[x] == s[y] and s[x] != s[Gazda]:
                    return self.fail('%s and %s withouth %s' % (x,y,Gazda))

        for o,b in s.items():
            if b != Vpravo:
                return self.fail('%s did not end on the right!' % o)
        return True

    def checkBad(self, r):
        if len(r) != 0:
            return self.fail('non-empty result when there should be no solution')
        return True

    def check(self, s0, n, good, r):
        if good:
            return self.checkGood(s0, n, r)
        else:
            return self.checkBad(r)


    def test(self, s0, n, good):
        self.case += 1
        self.tested += 1
        print('Case %d: %s (%d)' % (self.case, s0, n))

        try:
            start = now()
            r = VlkKozaKapusta().vyries(s0, n)
            duration = now() - start
        except KeyboardInterrupt:
            raise KeyboardInterrupt()
        except:
            printException()
            if stopOnError:
                raise FailedTestException()
            return

        print('  result: %s' % (r,))
        if self.check(s0, n, good, r):
            self.passed += 1
            print('PASSED  duration %s' % duration)
            print()
        else:
            print()
            if stopOnError:
                raise FailedTestException()

    def status(self):
        print("self.tested %d" % (self.tested,))
        print("self.passed %d" % (self.passed,))
        if self.tested == self.passed:
            print("OK")
            return True
        else:
            print("ERROR")
            return False

t = Tester()
try:
    s0 = {
        Vlk: Vpravo,
        Koza: Vpravo,
        Kapusta: Vpravo,
        Gazda: Vpravo
    }
    t.test(s0, 0, True)

    s0 = {
        Vlk: Vpravo,
        Koza: Vpravo,
        Kapusta: Vpravo,
        Gazda: Vpravo
    }
    t.test(s0, 1, False)

    s0 = {
        Vlk: Vpravo,
        Koza: Vlavo,
        Kapusta: Vpravo,
        Gazda: Vlavo
    }
    t.test(s0, 1, True)

    s0 = {
        Vlk: Vpravo,
        Koza: Vpravo,
        Kapusta: Vpravo,
        Gazda: Vpravo
    }
    t.test(s0, 2, True)

    s0 = {
        Vlk: Vpravo,
        Koza: Vlavo,
        Kapusta: Vpravo,
        Gazda: Vpravo
    }
    t.test(s0, 2, True)

    s0 = {
        Vlk: Vpravo,
        Koza: Vpravo,
        Kapusta: Vlavo,
        Gazda: Vpravo
    }
    t.test(s0, 2, False)

    s0 = {
        Vlk: Vlavo,
        Koza: Vpravo,
        Kapusta: Vpravo,
        Gazda: Vpravo
    }
    t.test(s0, 2, False)

    s0 = {
        Vlk: Vlavo,
        Koza: Vlavo,
        Kapusta: Vlavo,
        Gazda: Vlavo
    }
    t.test(s0, 7, True)

    s0 = {
        Vlk: Vlavo,
        Koza: Vlavo,
        Kapusta: Vlavo,
        Gazda: Vlavo
    }
    t.test(s0, 9, True)

    print("END")

except FailedTestException:
    print("Stopped on first failed test!")
finally:
    sys.exit(0 if t.status() else 1)

# vim: set sw=4 ts=4 sts=4 et :
