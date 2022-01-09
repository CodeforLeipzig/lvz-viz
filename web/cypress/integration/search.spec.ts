describe('LVZ search tests', () => {
  before(() => {
    cy.visit('/');
  })

  it('Find search input field', () => {
    cy.get('mat-tab-group [role="tab"]').contains('Suche').click();
    cy.get('input[data-placeholder="Suchphrase eingeben"]').type('Leipzig')
      .should('have.value', 'Leipzig');
  })

  it('Check map exists', () => {
    cy.get('#map-search').should('be.visible');
  })
})
