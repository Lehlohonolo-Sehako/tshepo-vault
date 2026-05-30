import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './verifier-api-key.reducer';

export const VerifierApiKeyDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const verifierApiKeyEntity = useAppSelector(state => state.verifierApiKey.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="verifierApiKeyDetailsHeading">Verifier Api Key</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{verifierApiKeyEntity.id}</dd>
          <dt>
            <span id="ownerLogin">Owner Login</span>
          </dt>
          <dd>{verifierApiKeyEntity.ownerLogin}</dd>
          <dt>
            <span id="label">Label</span>
          </dt>
          <dd>{verifierApiKeyEntity.label}</dd>
          <dt>
            <span id="keyHash">Key Hash</span>
          </dt>
          <dd>{verifierApiKeyEntity.keyHash}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{verifierApiKeyEntity.active ? 'true' : 'false'}</dd>
          <dt>
            <span id="callCount">Call Count</span>
          </dt>
          <dd>{verifierApiKeyEntity.callCount}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {verifierApiKeyEntity.createdAt ? (
              <TextFormat value={verifierApiKeyEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
        </dl>
        <Button as={Link as any} to="/verifier-api-key" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/verifier-api-key/${verifierApiKeyEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default VerifierApiKeyDetail;
