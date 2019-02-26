#!/usr/bin/env /bin/bash

clNorm=$'\e[0m'
clRed=$'\e[31m'
clLtRed=$'\e[31;1m'
clYellow=$'\e[33m'
clLtYellow=$'\e[33;1m'
clGreen=$'\e[32m'
clLtGreen=$'\e[32;1m'
die()  { echo; echo "${clLtRed}CHYBA${clNorm}: $*" >&2 ; echo >&2; exit 1 ; }
msg()  { echo  "$clGreen *$clNorm $*" ; }
err()  { echo    "$clRed *$clNorm $*" ; }
warn() { echo "$clYellow *$clNorm $*" ; }

TASKS_DIR="prakticke"

changedInPr() {
	local dirs="$*"
	git diff --name-only   ${TRAVIS_COMMIT_RANGE}  | awk -F/  " /${TASKS_DIR}\/${TRAVIS_BRANCH}\/(${dirs// /|})\// { print \$3}" | uniq | tr "\n" " "
}

runTest_python() {
	cd "$1" && ./*Test.py
}

runTest_java() {
	cd "$1" && gradle --console plain --quiet run
}

runTest_cpp() {
	cd "$1" && mkdir -p build && cd build && cmake .. && cmake --build . --target run
}

testLangsDefault() {
	find . -maxdepth 1 -mindepth 1 -type d | sed -e "s,^./,,"
}

runTestsForChanged() {
	local haveTest=false
	local passed=true
	for d in $(changedInPr "$@") ; do
		echo
		msg "Našiel som zmeny v $d riešení"
		lang="${d##*-}"
		task="${d%-*}"
		haveTest=true
		local run="runTest_$lang"

		echo "lang $lang task $task run $run"
		[[ $(type -t "${run}") == "function" ]] || die "Neviem ako testovať $d"
		msg "Testujem $d"
		echo
		if ( ${run} "$d" ) ; then
			echo
			msg "$d ${clLtGreen}OK${clNorm}"
		else
			echo
			err "${clLtRed}$d test zlyhal${clNorm}"
			passed=false;
		fi
	done
	${haveTest} || die "Nepodarilo sa identifikovať zmeny (riešenie) v žiadnom podporovanom jazyku"
	${passed} || die "Niektoré testy zlyhali"
}

# vim: set sw=4 sts=4 ts=4 noet :
