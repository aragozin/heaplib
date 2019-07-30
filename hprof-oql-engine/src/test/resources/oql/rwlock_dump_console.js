function forEach(list, code) {
    return length(map(list, code));
}

function printThread(thread) {
    return "#" + thread.tid + " " + thread.name.toString();
}

function forEachThreadLocal(callback) {

    forEach(heap.objects(heap.findClass("java.lang.Thread"), true),
        function(t) {
            if (t.threadLocals != null) {
                forEach(t.threadLocals.table, function(tle) {
                    if (tle != null && tle.referent != null) {
                        callback(t, tle.referent, tle.value);
                    }
                });
            }
        }
    )
}

var threadLocal = [];

forEachThreadLocal(function (t,r,v) { threadLocal.push({thread: t, ref: r, val: v});});

function reportLock(rwlock) {
    var lockinfo = {};
    lockinfo.lock = rwlock;
    lockinfo.writeowner = rwlock.writerLock.sync.exclusiveOwnerThread;
    lockinfo.readers = [];
    lockinfo.waiters = [];

    function walk_waiters(node) {
        if (node != null) {
            if (node.thread != null) {
                lockinfo.waiters.push(node.thread);
            }
            walk_waiters(node.next);
        }
    }

    walk_waiters(rwlock.writerLock.sync.head);

    if (rwlock.sync.firstReader != null) {
        lockinfo.readers.push(rwlock.sync.firstReader);
    }

	threadLocal.forEach(
        function(tlv) {
            if (identical(tlv.ref, rwlock.sync.readHolds) && tlv.val.count > 0) {
                lockinfo.readers.push(tlv.thread);
            }
        }
    )

    if (lockinfo.writeowner != undefined || lockinfo.readers.length > 0 || lockinfo.waiters.length > 0) {
        print("Lock #" + objectid(lockinfo.lock));
        if (lockinfo.writeowner) {
            print("  Owner: " + printThread(lockinfo.writeowner));
        }
        if (lockinfo.readers.length > 0) {
            print("  Reader: " + map(lockinfo.readers, printThread));
        }
        if (lockinfo.waiters.length > 0) {
            print("  Waiters: " + map(lockinfo.waiters, printThread));
        }
        print("");
    }

    return lockinfo;
}

length(map(
    heap.objects(heap.findClass("java.util.concurrent.locks.ReentrantReadWriteLock"), true),
    reportLock
   ));

""