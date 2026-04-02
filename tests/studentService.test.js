const StudentService = require('../src/services/studentService');

describe('StudentService', () => {
  let studentService;

  beforeEach(() => {
    studentService = new StudentService();
  });

  describe('updateStudent', () => {
    test('should update student properties correctly', () => {
      const student = studentService.addStudent('John Doe', 'john@example.com', 20);
      const originalId = student.id;
      
      studentService.updateStudent(student.id, {
        name: 'John Updated',
        age: 21
      });
      
      const updatedStudent = studentService.getStudentById(student.id);
      expect(updatedStudent.name).toBe('John Updated');
      expect(updatedStudent.age).toBe(21);
      expect(updatedStudent.id).toBe(originalId); // ID should remain the same
      expect(updatedStudent.email).toBe('john@example.com'); // Other properties should remain
    });

    test('should throw error when updating non-existent student', () => {
      expect(() => {
        studentService.updateStudent('non-existent-id', { name: 'Test' });
      }).toThrow('Student not found');
    });
  });

  describe('findByName', () => {
    test('should return empty array when name is null', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      const result = studentService.findByName(null);
      expect(result).toEqual([]);
    });

    test('should return empty array when name is undefined', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      const result = studentService.findByName(undefined);
      expect(result).toEqual([]);
    });

    test('should find students by partial name match', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      studentService.addStudent('Jane Smith', 'jane@example.com', 22);
      studentService.addStudent('Johnny Appleseed', 'johnny@example.com', 25);
      
      const result = studentService.findByName('john');
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Johnny Appleseed');
    });
  });

  describe('getByAgeRange', () => {
    test('should throw error when minAge is null', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(() => {
        studentService.getByAgeRange(null, 25);
      }).toThrow('Both minAge and maxAge are required');
    });

    test('should throw error when maxAge is null', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(() => {
        studentService.getByAgeRange(20, null);
      }).toThrow('Both minAge and maxAge are required');
    });

    test('should throw error when minAge > maxAge', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(() => {
        studentService.getByAgeRange(25, 20);
      }).toThrow('minAge must be less than or equal to maxAge');
    });

    test('should throw error when minAge is negative', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(() => {
        studentService.getByAgeRange(-5, 25);
      }).toThrow('Age values cannot be negative');
    });

    test('should throw error when maxAge is negative', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(() => {
        studentService.getByAgeRange(15, -5);
      }).toThrow('Age values cannot be negative');
    });

    test('should return students within age range', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      studentService.addStudent('Jane Smith', 'jane@example.com', 22);
      studentService.addStudent('Bob Johnson', 'bob@example.com', 18);
      studentService.addStudent('Alice Brown', 'alice@example.com', 25);
      
      const result = studentService.getByAgeRange(19, 23);
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Jane Smith');
    });
  });

  describe('basic functionality', () => {
    test('should add and retrieve students', () => {
      const student = studentService.addStudent('John Doe', 'john@example.com', 20);
      expect(student.name).toBe('John Doe');
      expect(student.email).toBe('john@example.com');
      expect(student.age).toBe(20);
      
      const retrieved = studentService.getStudentById(student.id);
      expect(retrieved).toEqual(student);
    });

    test('should get all students', () => {
      studentService.addStudent('John Doe', 'john@example.com', 20);
      studentService.addStudent('Jane Smith', 'jane@example.com', 22);
      
      const allStudents = studentService.getAllStudents();
      expect(allStudents).toHaveLength(2);
      expect(allStudents[0].name).toBe('John Doe');
      expect(allStudents[1].name).toBe('Jane Smith');
    });

    test('should delete students', () => {
      const student = studentService.addStudent('John Doe', 'john@example.com', 20);
      
      expect(studentService.deleteStudent(student.id)).toBe(true);
      expect(studentService.getStudentById(student.id)).toBeUndefined();
    });
  });
});
