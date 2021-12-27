describe('LVZ statistic tests', () => {
  before(() => {
    cy.visit('/');
  })

  it('Find buttons', () => {
    cy.get('mat-tab-group [role="tab"]').contains('Statistik').click();
    cy.get('button').should('have.length', 2).and('have.class', 'mat-primary');
  })

  it('Check map exists', () => {
    cy.get('#map-statistic').should('be.visible');
  })
})
