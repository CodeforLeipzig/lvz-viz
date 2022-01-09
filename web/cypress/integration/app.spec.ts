describe('LVZ tests', () => {
  before(() => {
    cy.visit('/');
  })

  it('Check app title in first of two toolbars', () => {
    cy.get('mat-toolbar').should('have.length', 2)
      .eq(0).should('contain', 'LVZ Polizeiticker');
  })

  it('Check tab count and check tab one is "Suche"', () => {
    cy.get('mat-tab-group [role="tab"]').should('have.length', 2)
      .eq(0).should('contain', 'Suche');
  })

  it('Check table exists', () => {
    cy.get('mat-table').should('be.visible');
  })
})
