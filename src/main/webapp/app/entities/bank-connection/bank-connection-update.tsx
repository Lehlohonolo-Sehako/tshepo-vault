import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { BankConnectionStatus } from 'app/shared/model/enumerations/bank-connection-status.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './bank-connection.reducer';

export const BankConnectionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const bankConnectionEntity = useAppSelector(state => state.bankConnection.entity);
  const loading = useAppSelector(state => state.bankConnection.loading);
  const updating = useAppSelector(state => state.bankConnection.updating);
  const updateSuccess = useAppSelector(state => state.bankConnection.updateSuccess);
  const bankConnectionStatusValues = Object.keys(BankConnectionStatus);

  const handleClose = () => {
    navigate('/bank-connection');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    values.connectedAt = convertDateTimeToServer(values.connectedAt);

    const entity = {
      ...bankConnectionEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          connectedAt: displayDefaultDateTime(),
        }
      : {
          status: 'CONNECTED',
          ...bankConnectionEntity,
          connectedAt: convertDateTimeFromServer(bankConnectionEntity.connectedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tshepoVaultApp.bankConnection.home.createOrEditLabel" data-cy="BankConnectionCreateUpdateHeading">
            Create or edit a Bank Connection
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="bank-connection-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Holder Login"
                id="bank-connection-holderLogin"
                name="holderLogin"
                data-cy="holderLogin"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 50, message: 'This field cannot be longer than 50 characters.' },
                }}
              />
              <ValidatedField
                label="Connected At"
                id="bank-connection-connectedAt"
                name="connectedAt"
                data-cy="connectedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField label="Status" id="bank-connection-status" name="status" data-cy="status" type="select">
                {bankConnectionStatusValues.map(bankConnectionStatus => (
                  <option value={bankConnectionStatus} key={bankConnectionStatus}>
                    {bankConnectionStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Masked Account"
                id="bank-connection-maskedAccount"
                name="maskedAccount"
                data-cy="maskedAccount"
                type="text"
                validate={{
                  maxLength: { value: 20, message: 'This field cannot be longer than 20 characters.' },
                }}
              />
              <OverlayTrigger overlay={<Tooltip>Masked account number shown in the UI, e.g. &#34;•••• 4821&#34;.</Tooltip>}>
                <span id="maskedAccountLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField
                label="Account Type"
                id="bank-connection-accountType"
                name="accountType"
                data-cy="accountType"
                type="text"
                validate={{
                  maxLength: { value: 80, message: 'This field cannot be longer than 80 characters.' },
                }}
              />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/bank-connection" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default BankConnectionUpdate;
