const { v4: uuidv4 } = require('uuid');

class StudentService {
  constructor() {
    this.students = new Map();
  }

  // Add a new student
  addStudent(name, email, age) {
    const studentId = uuidv4();
    const student = {
      id: studentId,
      name,
      email,
      age,
      createdAt: new Date()
    };
    
    this.students.set(studentId, student);
    return student;
  }

  // Get all students
  getAllStudents() {
    return Array.from(this.students.values());
  }

  // Get student by ID
  getStudentById(id) {
    return this.students.get(id);
  }

  // Update student - FIXED: Use Object.assign instead of variable reassignment
  updateStudent(id, updates) {
    const student = this.students.get(id);
    if (!student) {
      throw new Error('Student not found');
    }
    
    Object.assign(student, updates);
    this.students.set(id, student);
    return student;
  }

  // Delete student
  deleteStudent(id) {
    const deleted = this.students.delete(id);
    if (!deleted) {
      throw new Error('Student not found');
    }
    return true;
  }

  // Find students by name - FIXED: Added null/undefined check
  findByName(name) {
    if (name == null) return [];
    return this.getAllStudents().filter(student => 
      student.name.toLowerCase().includes(name.toLowerCase())
    );
  }

  // Get students by age range - FIXED: Added validation
  getByAgeRange(minAge, maxAge) {
    if (!minAge || !maxAge) {
      throw new Error('Both minAge and maxAge are required');
    }
    
    if (minAge > maxAge) {
      throw new Error('minAge must be less than or equal to maxAge');
    }
    
    if (minAge < 0 || maxAge < 0) {
      throw new Error('Age values cannot be negative');
    }
    
    return this.getAllStudents().filter(student => 
      student.age >= minAge && student.age <= maxAge
    );
  }
}

module.exports = StudentService;
