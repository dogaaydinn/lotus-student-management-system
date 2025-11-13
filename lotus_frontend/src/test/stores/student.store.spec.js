import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import useStudentStore from '../../stores/student.store'

describe('Student Store', () => {
  beforeEach(() => {
    // Create a fresh pinia instance for each test
    setActivePinia(createPinia())
  })

  it('should initialize with null student', () => {
    // Given
    const store = useStudentStore()

    // Then
    expect(store.student).toBeNull()
    expect(store.isStudentLoggedIn).toBe(false)
  })

  it('should update isStudentLoggedIn when student is set', () => {
    // Given
    const store = useStudentStore()

    // When
    store.student = {
      id: 1,
      username: 'teststudent',
      name: 'Test',
      surname: 'Student',
      email: 'test@student.com'
    }

    // Then
    expect(store.isStudentLoggedIn).toBe(true)
  })

  it('should logout student correctly', () => {
    // Given
    const store = useStudentStore()
    store.student = {
      id: 1,
      username: 'teststudent',
      name: 'Test',
      surname: 'Student'
    }

    // When
    store.logoutStudent()

    // Then
    expect(store.student).toBeNull()
    expect(store.isStudentLoggedIn).toBe(false)
  })

  it('should handle multiple login/logout cycles', () => {
    // Given
    const store = useStudentStore()

    // Cycle 1
    store.student = { id: 1, username: 'student1' }
    expect(store.isStudentLoggedIn).toBe(true)

    store.logoutStudent()
    expect(store.isStudentLoggedIn).toBe(false)

    // Cycle 2
    store.student = { id: 2, username: 'student2' }
    expect(store.isStudentLoggedIn).toBe(true)

    store.logoutStudent()
    expect(store.isStudentLoggedIn).toBe(false)
  })

  it('should maintain student data integrity', () => {
    // Given
    const store = useStudentStore()
    const studentData = {
      id: 1,
      username: 'teststudent',
      name: 'Test',
      surname: 'Student',
      email: 'test@student.com',
      faculty: 'Engineering',
      department: 'Computer Science',
      gpa: 3.5
    }

    // When
    store.student = studentData

    // Then
    expect(store.student.id).toBe(1)
    expect(store.student.username).toBe('teststudent')
    expect(store.student.email).toBe('test@student.com')
    expect(store.student.gpa).toBe(3.5)
  })
})
