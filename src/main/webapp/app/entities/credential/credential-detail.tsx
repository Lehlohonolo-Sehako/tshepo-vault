import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './credential.reducer';

export const CredentialDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const credentialEntity = useAppSelector(state => state.credential.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="credentialDetailsHeading">Credential</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{credentialEntity.id}</dd>
          <dt>
            <span id="holderLogin">Holder Login</span>
          </dt>
          <dd>{credentialEntity.holderLogin}</dd>
          <dt>
            <span id="title">Title</span>
          </dt>
          <dd>{credentialEntity.title}</dd>
          <dt>
            <span id="purpose">Purpose</span>
          </dt>
          <dd>{credentialEntity.purpose}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{credentialEntity.status}</dd>
          <dt>
            <span id="issuedAt">Issued At</span>
          </dt>
          <dd>
            {credentialEntity.issuedAt ? <TextFormat value={credentialEntity.issuedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="expiresAt">Expires At</span>
          </dt>
          <dd>
            {credentialEntity.expiresAt ? <TextFormat value={credentialEntity.expiresAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="issuerDid">Issuer Did</span>
          </dt>
          <dd>{credentialEntity.issuerDid}</dd>
          <dt>
            <span id="sdJwt">Sd Jwt</span>
          </dt>
          <dd>{credentialEntity.sdJwt}</dd>
          <dt>
            <span id="claimsSummary">Claims Summary</span>
            <OverlayTrigger overlay={<Tooltip>Comma-separated short claim labels for list-view display only.</Tooltip>}>
              <span id="claimsSummary" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{credentialEntity.claimsSummary}</dd>
          <dt>
            <span id="vcRef">Vc Ref</span>
          </dt>
          <dd>{credentialEntity.vcRef}</dd>
        </dl>
        <Button as={Link as any} to="/credential" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/credential/${credentialEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CredentialDetail;
