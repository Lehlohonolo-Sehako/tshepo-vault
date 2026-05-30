import React from 'react';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/credential">
        Credential
      </MenuItem>
      <MenuItem icon="asterisk" to="/issued-claim">
        Issued Claim
      </MenuItem>
      <MenuItem icon="asterisk" to="/verifier-api-key">
        Verifier Api Key
      </MenuItem>
      <MenuItem icon="asterisk" to="/verification-event">
        Verification Event
      </MenuItem>
      <MenuItem icon="asterisk" to="/bank-connection">
        Bank Connection
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
