// Tests the behavior of getter and setter entries.

// Both getters and setters
class A {
  prop = 0;

  //- @foo defines/binding PropFoo=VName("A.foo", _, _, _, _)
  //- PropFoo.node/kind variable
  //- PropFoo.subkind implicit
  //- @foo defines/binding GetFoo=VName("A.foo:getter", _, _, _, _)
  //- GetFoo.node/kind function
  //- GetFoo property/reads PropFoo
  get foo() {
    return this.prop;
  }

  //- @foo defines/binding SetFoo=VName("A.foo:setter", _, _, _, _)
  //- SetFoo.node/kind function
  //- SetFoo property/writes PropFoo
  set foo(nFoo) {
    this.prop = nFoo;
  }

  method() {
    //- @foo ref GetFoo
    this.foo;
    //- @foo ref GetFoo
    this.foo = 0;
  }
}

// Only getters
class B {
  iProp = 0;

  //- @prop defines/binding PropProp=VName("B.prop", _, _, _, _)
  //- PropProp.node/kind variable
  //- PropProp.subkind implicit
  //- @prop defines/binding GetProp=VName("B.prop:getter", _, _, _, _)
  //- GetProp.node/kind function
  //- GetProp property/reads PropProp
  get prop() {
    return this.iProp;
  }

  method() {
    //- @prop ref GetProp
    this.prop;
  }
}

// Only setters
class C {
  prop = 0;

  //- @mem defines/binding PropMem=VName("C.mem", _, _, _, _)
  //- PropMem.node/kind variable
  //- PropMem.subkind implicit
  //- @mem defines/binding SetMem=VName("C.mem:setter", _, _, _, _)
  //- SetMem.node/kind function
  //- SetMem property/writes PropMem
  set mem(nMem) {
    this.prop = nMem;
  }

  method() {
    //- @mem ref SetMem
    this.mem;
    //- @mem ref SetMem
    this.mem = 0;
  }
}
