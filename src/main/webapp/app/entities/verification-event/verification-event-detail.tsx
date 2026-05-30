import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './verification-event.reducer';

export const VerificationEventDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const verificationEventEntity = useAppSelector(state => state.verificationEvent.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="verificationEventDetailsHeading">Verification Event</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{verificationEventEntity.id}</dd>
          <dt>
            <span id="verifiedAt">Verified At</span>
          </dt>
          <dd>
            {verificationEventEntity.verifiedAt ? (
              <TextFormat value={verificationEventEntity.verifiedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="result">Result</span>
          </dt>
          <dd>{verificationEventEntity.result}</dd>
          <dt>
            <span id="disclosedClaims">Disclosed Claims</span>
            <OverlayTrigger overlay={<Tooltip>JSON array of ClaimType strings that were disclosed.</Tooltip>}>
              <span id="disclosedClaims" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{verificationEventEntity.disclosedClaims}</dd>
          <dt>
            <span id="credentialRef">Credential Ref</span>
            <OverlayTrigger overlay={<Tooltip>URN reference to the credential that was presented (for audit).</Tooltip>}>
              <span id="credentialRef" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{verificationEventEntity.credentialRef}</dd>
          <dt>Api Key</dt>
          <dd>{verificationEventEntity.apiKey ? verificationEventEntity.apiKey.label : ''}</dd>
        </dl>
        <Button as={Link as any} to="/verification-event" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/verification-event/${verificationEventEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default VerificationEventDetail;
