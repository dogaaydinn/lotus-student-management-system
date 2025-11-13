// Custom Cypress commands

Cypress.Commands.add('login', (username, password, role) => {
  cy.request('POST', 'http://localhost:8085/api/auth/login', {
    username,
    password,
    role
  }).then((response) => {
    window.localStorage.setItem('token', response.body.token)
  })
})
