import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './verifier-api-key.reducer';

export const VerifierApiKeyUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const verifierApiKeyEntity = useAppSelector(state => state.verifierApiKey.entity);
  const loading = useAppSelector(state => state.verifierApiKey.loading);
  const updating = useAppSelector(state => state.verifierApiKey.updating);
  const updateSuccess = useAppSelector(state => state.verifierApiKey.updateSuccess);

  const handleClose = () => {
    navigate('/verifier-api-key');
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
    if (values.callCount !== undefined && typeof values.callCount !== 'number') {
      values.callCount = Number(values.callCount);
    }
    values.createdAt = convertDateTimeToServer(values.createdAt);

    const entity = {
      ...verifierApiKeyEntity,
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
          createdAt: displayDefaultDateTime(),
        }
      : {
          ...verifierApiKeyEntity,
          createdAt: convertDateTimeFromServer(verifierApiKeyEntity.createdAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tshepoVaultApp.verifierApiKey.home.createOrEditLabel" data-cy="VerifierApiKeyCreateUpdateHeading">
            Create or edit a Verifier Api Key
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="verifier-api-key-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Owner Login"
                id="verifier-api-key-ownerLogin"
                name="ownerLogin"
                data-cy="ownerLogin"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 50, message: 'This field cannot be longer than 50 characters.' },
                }}
              />
              <ValidatedField
                label="Label"
                id="verifier-api-key-label"
                name="label"
                data-cy="label"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 80, message: 'This field cannot be longer than 80 characters.' },
                }}
              />
              <ValidatedField
                label="Key Hash"
                id="verifier-api-key-keyHash"
                name="keyHash"
                data-cy="keyHash"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 100, message: 'This field cannot be longer than 100 characters.' },
                }}
              />
              <ValidatedField label="Active" id="verifier-api-key-active" name="active" data-cy="active" check type="checkbox" />
              <ValidatedField
                label="Call Count"
                id="verifier-api-key-callCount"
                name="callCount"
                data-cy="callCount"
                type="text"
                validate={{
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Created At"
                id="verifier-api-key-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/verifier-api-key" replace variant="info">
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

export default VerifierApiKeyUpdate;
