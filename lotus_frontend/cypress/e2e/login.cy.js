describe('Login Flow', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('should display login page', () => {
    cy.contains('Login').should('be.visible')
  })

  it('should handle login validation', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('required').should('be.visible')
  })
})
