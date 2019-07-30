length(map(
	filter(
	    heap.roots(),
	    function(r) {
	        return root(r) && root(r).type == "thread object";
	    }
	),
    function(t) {

		print("#\n" + t.id + " " + t.name.toString());
        Java.type("java.util.Arrays").asList(root(t).wrapped.getStackTrace()).forEach(function(e){
			print("  " + e);
		});
    }
))

null