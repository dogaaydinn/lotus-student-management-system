# Contributing to Lotus Student Management System

First off, thank you for considering contributing to Lotus SMS! It's people like you that make this project such a great tool.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)

---

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

---

## Getting Started

### Prerequisites

- Java 8+
- Node.js 18+
- Maven 3.6+
- MySQL 8.0+ (or Docker)
- Git
- IDE (IntelliJ IDEA recommended for backend, VS Code for frontend)

### Setup Development Environment

1. **Fork the repository**
   ```bash
   # Click 'Fork' button on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/lotus-student-management-system.git
   cd lotus-student-management-system
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/dogaaydinn/lotus-student-management-system.git
   ```

4. **Backend Setup**
   ```bash
   cd lotos_backend
   cp .env.example .env
   # Edit .env with your configuration
   mvn clean install
   mvn spring-boot:run
   ```

5. **Frontend Setup**
   ```bash
   cd lotus_frontend
   npm install
   npm run dev
   ```

---

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Screenshots** (if applicable)
- **Environment details** (OS, Java version, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. Include:

- **Use case description**
- **Detailed explanation**
- **Mockups/diagrams** (if applicable)
- **Alternative solutions** considered

### Your First Code Contribution

Unsure where to begin? Look for:

- `good first issue` - Good for newcomers
- `help wanted` - Issues needing attention
- `documentation` - Documentation improvements

---

## Development Workflow

### 1. Create a Branch

```bash
# Update your local main
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/amazing-feature

# Or for bug fixes
git checkout -b fix/bug-description
```

### 2. Make Changes

- Write code following our coding standards
- Add/update tests
- Update documentation
- Ensure all tests pass

### 3. Commit Changes

```bash
# Stage changes
git add .

# Commit with conventional commit message
git commit -m "feat: add amazing feature"
```

### 4. Push to Fork

```bash
git push origin feature/amazing-feature
```

### 5. Create Pull Request

- Go to your fork on GitHub
- Click "New Pull Request"
- Fill in the PR template
- Link related issues

---

## Coding Standards

### Java (Backend)

```java
// Follow Java Code Conventions
// Use meaningful variable names
public class StudentService {

    // Javadoc for public methods
    /**
     * Retrieves a student by ID.
     *
     * @param id the student ID
     * @return the student entity
     * @throws ResourceNotFoundException if student not found
     */
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    // Use Spring annotations appropriately
    @Transactional
    @Cacheable(value = "students", key = "#id")
    public Student getCachedStudent(Long id) {
        return getStudentById(id);
    }
}
```

**Key Points:**
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use `@Override` annotation
- Handle exceptions appropriately
- Use Lombok where appropriate
- Follow SOLID principles

### JavaScript/Vue.js (Frontend)

```javascript
// Use Composition API
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'StudentList',
  setup() {
    const students = ref([])
    const loading = ref(false)

    const activeStudents = computed(() => {
      return students.value.filter(s => s.active)
    })

    onMounted(async () => {
      await fetchStudents()
    })

    const fetchStudents = async () => {
      loading.value = true
      try {
        const response = await studentApi.getAll()
        students.value = response.data
      } catch (error) {
        console.error('Failed to fetch students:', error)
      } finally {
        loading.value = false
      }
    }

    return {
      students,
      loading,
      activeStudents,
      fetchStudents
    }
  }
}
```

**Key Points:**
- Use 2 spaces for indentation
- Use ESLint + Prettier
- Prefer `const` over `let`
- Use async/await over promises
- Handle errors gracefully
- Use Composition API over Options API

### Database Migrations

```sql
-- V4__Add_Student_GPA.sql
-- Add GPA column to student table

ALTER TABLE student
ADD COLUMN gpa DECIMAL(3,2) DEFAULT NULL
COMMENT 'Student Grade Point Average';

-- Add index for GPA queries
CREATE INDEX idx_student_gpa ON student(gpa);

-- Update existing records (if needed)
UPDATE student SET gpa = 0.00 WHERE gpa IS NULL;
```

**Key Points:**
- Follow Flyway naming convention: `V{version}__{description}.sql`
- Include comments
- Make migrations idempotent when possible
- Test rollback scenarios

---

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding/updating tests
- `chore`: Build process, dependencies, etc.
- `ci`: CI configuration changes

### Examples

```bash
# Feature
git commit -m "feat(student): add GPA calculation functionality"

# Bug fix
git commit -m "fix(auth): resolve JWT token expiration issue"

# Documentation
git commit -m "docs(api): update authentication endpoint documentation"

# With body
git commit -m "feat(notifications): add email notification system

Implements email notifications for:
- Application status changes
- New messages
- Internship deadlines

Closes #123"
```

---

## Pull Request Process

### Before Submitting

- [ ] Run all tests: `mvn test` (backend), `npm test` (frontend)
- [ ] Check code coverage: should not decrease
- [ ] Run linters: `mvn checkstyle:check`, `npm run lint`
- [ ] Update documentation
- [ ] Add/update tests for new features
- [ ] Ensure CI passes
- [ ] Rebase on latest main

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe testing performed

## Checklist
- [ ] Tests pass locally
- [ ] Added/updated tests
- [ ] Updated documentation
- [ ] Followed coding standards
- [ ] No new warnings

## Screenshots (if applicable)

## Related Issues
Closes #123
```

### Review Process

1. **Automated Checks**: CI must pass
2. **Code Review**: At least one approval required
3. **Testing**: Reviewer should test changes
4. **Merge**: Maintainer will merge

---

## Testing Guidelines

### Backend Tests

```java
@Test
void testCreateStudent() {
    // Given
    Student student = new Student();
    student.setUsername("testuser");
    student.setEmail("test@example.com");

    // When
    Student saved = studentService.create(student);

    // Then
    assertNotNull(saved.getId());
    assertEquals("testuser", saved.getUsername());
}

@Test
void testGetStudent_NotFound() {
    // When/Then
    assertThrows(ResourceNotFoundException.class, () -> {
        studentService.getById(999L);
    });
}
```

### Frontend Tests

```javascript
import { mount } from '@vue/test-utils'
import StudentList from '@/views/StudentList.vue'

describe('StudentList.vue', () => {
  it('renders students when data is loaded', async () => {
    const wrapper = mount(StudentList)

    await wrapper.vm.fetchStudents()

    expect(wrapper.findAll('.student-item')).toHaveLength(3)
  })
})
```

### Test Coverage Goals

- **Backend**: Minimum 80% coverage
- **Frontend**: Minimum 70% coverage
- **Critical paths**: 100% coverage

---

## Additional Resources

- [Architecture Documentation](./docs/ARCHITECTURE.md)
- [API Documentation](http://localhost:8085/swagger-ui.html)
- [Development Guide](./docs/DEVELOPMENT.md)
- [Roadmap](./ROADMAP.md)

---

## Questions?

- **GitHub Discussions**: https://github.com/dogaaydinn/lotus-student-management-system/discussions
- **Email**: support@lotus-spm.com

---

Thank you for contributing! 🌸
