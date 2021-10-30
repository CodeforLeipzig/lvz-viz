describe('My First Test', () => {
  it('Visits the initial project page', () => {
    cy.visit('/')
    cy.contains('Hello world')
    cy.contains('hello-world works!')
  })
})
