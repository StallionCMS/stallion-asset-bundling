#!/usr/local/bin/python3

from plumbum import local, FG

args = []
args = tuple(args) + ('compile', 'package', 'install')
local['mvn'][args] & FG

