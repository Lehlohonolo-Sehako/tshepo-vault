import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './issued-claim.reducer';

export const IssuedClaimDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const issuedClaimEntity = useAppSelector(state => state.issuedClaim.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="issuedClaimDetailsHeading">Issued Claim</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{issuedClaimEntity.id}</dd>
          <dt>
            <span id="claimType">Claim Type</span>
          </dt>
          <dd>{issuedClaimEntity.claimType}</dd>
          <dt>
            <span id="operator">Operator</span>
          </dt>
          <dd>{issuedClaimEntity.operator}</dd>
          <dt>
            <span id="threshold">Threshold</span>
            <OverlayTrigger overlay={<Tooltip>Threshold value (Rand for monetary types, months for TENURE).</Tooltip>}>
              <span id="threshold" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{issuedClaimEntity.threshold}</dd>
          <dt>
            <span id="currency">Currency</span>
          </dt>
          <dd>{issuedClaimEntity.currency}</dd>
          <dt>
            <span id="periodMonths">Period Months</span>
            <OverlayTrigger overlay={<Tooltip>Look-back window used when computing this claim.</Tooltip>}>
              <span id="periodMonths" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{issuedClaimEntity.periodMonths}</dd>
          <dt>
            <span id="met">Met</span>
            <OverlayTrigger overlay={<Tooltip>True if the holder satisfied this threshold at issuance time.</Tooltip>}>
              <span id="met" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{issuedClaimEntity.met ? 'true' : 'false'}</dd>
          <dt>Credential</dt>
          <dd>{issuedClaimEntity.credential ? issuedClaimEntity.credential.title : ''}</dd>
        </dl>
        <Button as={Link as any} to="/issued-claim" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/issued-claim/${issuedClaimEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default IssuedClaimDetail;
