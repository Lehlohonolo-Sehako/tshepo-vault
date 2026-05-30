import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { CredentialStatus } from 'app/shared/model/enumerations/credential-status.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './credential.reducer';

export const CredentialUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const credentialEntity = useAppSelector(state => state.credential.entity);
  const loading = useAppSelector(state => state.credential.loading);
  const updating = useAppSelector(state => state.credential.updating);
  const updateSuccess = useAppSelector(state => state.credential.updateSuccess);
  const credentialStatusValues = Object.keys(CredentialStatus);

  const handleClose = () => {
    navigate(`/credential${location.search}`);
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
    values.issuedAt = convertDateTimeToServer(values.issuedAt);
    values.expiresAt = convertDateTimeToServer(values.expiresAt);

    const entity = {
      ...credentialEntity,
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
          issuedAt: displayDefaultDateTime(),
          expiresAt: displayDefaultDateTime(),
        }
      : {
          status: 'ACTIVE',
          ...credentialEntity,
          issuedAt: convertDateTimeFromServer(credentialEntity.issuedAt),
          expiresAt: convertDateTimeFromServer(credentialEntity.expiresAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tshepoVaultApp.credential.home.createOrEditLabel" data-cy="CredentialCreateUpdateHeading">
            Create or edit a Credential
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="credential-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Holder Login"
                id="credential-holderLogin"
                name="holderLogin"
                data-cy="holderLogin"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 50, message: 'This field cannot be longer than 50 characters.' },
                }}
              />
              <ValidatedField
                label="Title"
                id="credential-title"
                name="title"
                data-cy="title"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 120, message: 'This field cannot be longer than 120 characters.' },
                }}
              />
              <ValidatedField
                label="Purpose"
                id="credential-purpose"
                name="purpose"
                data-cy="purpose"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField label="Status" id="credential-status" name="status" data-cy="status" type="select">
                {credentialStatusValues.map(credentialStatus => (
                  <option value={credentialStatus} key={credentialStatus}>
                    {credentialStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Issued At"
                id="credential-issuedAt"
                name="issuedAt"
                data-cy="issuedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Expires At"
                id="credential-expiresAt"
                name="expiresAt"
                data-cy="expiresAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Issuer Did"
                id="credential-issuerDid"
                name="issuerDid"
                data-cy="issuerDid"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Sd Jwt"
                id="credential-sdJwt"
                name="sdJwt"
                data-cy="sdJwt"
                type="textarea"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Claims Summary"
                id="credential-claimsSummary"
                name="claimsSummary"
                data-cy="claimsSummary"
                type="text"
                validate={{
                  maxLength: { value: 500, message: 'This field cannot be longer than 500 characters.' },
                }}
              />
              <OverlayTrigger overlay={<Tooltip>Comma-separated short claim labels for list-view display only.</Tooltip>}>
                <span id="claimsSummaryLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField
                label="Vc Ref"
                id="credential-vcRef"
                name="vcRef"
                data-cy="vcRef"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/credential" replace variant="info">
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

export default CredentialUpdate;
