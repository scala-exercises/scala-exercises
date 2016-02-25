
/*
From: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Introduction_to_Object-Oriented_JavaScript
 */
function Foo(){
    alert("Foo")
}

function Alert(msg){
    alert(msg)
}

// Define the Person constructor
function Person(firstName) {
  this.firstName = firstName;
}

var person1 = new Person('Alice');
var person2 = new Person('Bob');

// Add a couple of methods to Person.prototype
Person.prototype.walk = function(){
  alert("I am walking!");
};
Person.prototype.sayHello = function(){
  alert("Hello, I'm "+ this.firstName);
};

// Define the Student constructor
function Student(firstName, subject) {
  // Call the parent constructor, making sure (using Function#call) that "this" is
  // set correctly during the call
  Person.call(this, firstName);

  // Initialize our Student-specific properties
  this.subject = subject;
};

// Create a Student.prototype object that inherits from Person.prototype.
// Note: A common error here is to use "new Person()" to create the Student.prototype.
// That's incorrect for several reasons, not least that we don't have anything to
// give Person for the "firstName" argument. The correct place to call Person is
// above, where we call it from Student.
Student.prototype = Object.create(Person.prototype); // See note below

// Set the "constructor" property to refer to Student
Student.prototype.constructor = Student;

// Replace the "sayHello" method
Student.prototype.sayHello = function(){
  alert("Hello, I'm " + this.firstName + ". I'm studying " + this.subject + ".");
};

// Add a "sayGoodBye" method
Student.prototype.sayGoodBye = function(){
  alert("Goodbye!");
};

/*
// Example usage:
var student1 = new Student("Janet", "Applied Physics");
student1.sayHello();   // "Hello, I'm Janet. I'm studying Applied Physics."
student1.walk();       // "I am walking!"
student1.sayGoodBye(); // "Goodbye!"

// Check that instanceof works correctly
alert(student1 instanceof Person);  // true
alert(student1 instanceof Student); // true
*/