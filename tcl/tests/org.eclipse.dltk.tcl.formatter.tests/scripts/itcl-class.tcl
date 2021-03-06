====proc
itcl::class A {
proc m2 {} {
set x "m2"
}
}
==
itcl::class A {
	proc m2 {} {
		set x "m2"
	}
}
====methods
itcl::class A {
method m1 {} {
set x "m1"
}
proc m2 {} {
set x "m2"
}
public proc m3 {} {
set x "m3"
} 
protected proc m4 {} {
set x "m4"
}
private proc m5 {} {
set x "m5"
}
}
==
itcl::class A {
	method m1 {} {
		set x "m1"
	}
	proc m2 {} {
		set x "m2"
	}
	public proc m3 {} {
		set x "m3"
	}
	protected proc m4 {} {
		set x "m4"
	}
	private proc m5 {} {
		set x "m5"
	}
}
==== constructor
itcl::class A {
constructor {} {
set x "A-constuctor" 
}
destructor {
set x "A-destructor"
}
}
==
itcl::class A {
	constructor {} {
		set x "A-constuctor"
	}
	destructor {
		set x "A-destructor"
	}
}
==== class&body
itcl::class A {
method m1 {} {}
proc m2 {} {}
public proc m3 {} {}
protected proc m4 {} {}
private proc m5 {} {}
}
body A::m1 {} {
set x "m1"
}
body A::m2 {} {
set x "m2"
}
body A::m3 {} {
set x "m3"
}
body A::m4 {} {
set x "m4"
}
body A::m5 {} {
set x "m5"
}
==
itcl::class A {
	method m1 {} {}
	proc m2 {} {}
	public proc m3 {} {}
	protected proc m4 {} {}
	private proc m5 {} {}
}
body A::m1 {} {
	set x "m1"
}
body A::m2 {} {
	set x "m2"
}
body A::m3 {} {
	set x "m3"
}
body A::m4 {} {
	set x "m4"
}
body A::m5 {} {
	set x "m5"
}
==== visibility-blocks
itcl::class A {
public {
method m1 {} {
puts "m1"
}
proc m2 {} {
puts "m2"
}
}
}
==
itcl::class A {
	public {
		method m1 {} {
			puts "m1"
		}
		proc m2 {} {
			puts "m2"
		}
	}
}
