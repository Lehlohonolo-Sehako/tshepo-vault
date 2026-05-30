import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './bank-connection.reducer';

export const BankConnectionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const bankConnectionEntity = useAppSelector(state => state.bankConnection.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="bankConnectionDetailsHeading">Bank Connection</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{bankConnectionEntity.id}</dd>
          <dt>
            <span id="holderLogin">Holder Login</span>
          </dt>
          <dd>{bankConnectionEntity.holderLogin}</dd>
          <dt>
            <span id="connectedAt">Connected At</span>
          </dt>
          <dd>
            {bankConnectionEntity.connectedAt ? (
              <TextFormat value={bankConnectionEntity.connectedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{bankConnectionEntity.status}</dd>
          <dt>
            <span id="maskedAccount">Masked Account</span>
            <OverlayTrigger overlay={<Tooltip>Masked account number shown in the UI, e.g. &#34;•••• 4821&#34;.</Tooltip>}>
              <span id="maskedAccount" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{bankConnectionEntity.maskedAccount}</dd>
          <dt>
            <span id="accountType">Account Type</span>
          </dt>
          <dd>{bankConnectionEntity.accountType}</dd>
        </dl>
        <Button as={Link as any} to="/bank-connection" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/bank-connection/${bankConnectionEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default BankConnectionDetail;
