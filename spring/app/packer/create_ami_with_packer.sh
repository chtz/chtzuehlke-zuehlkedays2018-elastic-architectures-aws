#!/bin/bash
packer build txt2speech.json | tee packer.out
cat packer.out | tail -n 2 | head -n 1 | cut -d' ' -f 2 > ami.txt
