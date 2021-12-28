#!/usr/bin/env -S v run

// Copies mod jar from subprojects into one target folder

rmdir_all('target') or { }
mkdir('target') ?

files0 := ls('fabric/build/libs') ?
files1 := ls('forge/build/libs') ?

mut files := ls('.') ?

for file in files0 {
    files << 'fabric/build/libs/' + file
}

for file in files1 {
    files << 'forge/build/libs/' + file
}

mut count := 0
if files.len > 0 {
    for file in files {
        if file.ends_with('.jar') && !(file.contains('dev') || file.contains('sources')) {
            mut plat := "-unknown"
            if file.contains('fabric') {
                plat = '-Fabric-'
            } else {
                plat = '-Forge-'
            }

            cp(file, 'target/' + base(file).replace_once('-', plat)) or {
                println('err: $err')
                return
            }
        }
        count++
    }
}
if count == 0 {
     println('No files')
}