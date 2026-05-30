import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getVerifierApiKeys } from 'app/entities/verifier-api-key/verifier-api-key.reducer';
import { VerificationResult } from 'app/shared/model/enumerations/verification-result.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './verification-event.reducer';

export const VerificationEventUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const verifierApiKeys = useAppSelector(state => state.verifierApiKey.entities);
  const verificationEventEntity = useAppSelector(state => state.verificationEvent.entity);
  const loading = useAppSelector(state => state.verificationEvent.loading);
  const updating = useAppSelector(state => state.verificationEvent.updating);
  const updateSuccess = useAppSelector(state => state.verificationEvent.updateSuccess);
  const verificationResultValues = Object.keys(VerificationResult);

  const handleClose = () => {
    navigate(`/verification-event${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getVerifierApiKeys({}));
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
    values.verifiedAt = convertDateTimeToServer(values.verifiedAt);

    const entity = {
      ...verificationEventEntity,
      ...values,
      apiKey: verifierApiKeys.find(it => it.id.toString() === values.apiKey?.toString()),
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
          verifiedAt: displayDefaultDateTime(),
        }
      : {
          result: 'VALID',
          ...verificationEventEntity,
          verifiedAt: convertDateTimeFromServer(verificationEventEntity.verifiedAt),
          apiKey: verificationEventEntity?.apiKey?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tshepoVaultApp.verificationEvent.home.createOrEditLabel" data-cy="VerificationEventCreateUpdateHeading">
            Create or edit a Verification Event
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="verification-event-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Verified At"
                id="verification-event-verifiedAt"
                name="verifiedAt"
                data-cy="verifiedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField label="Result" id="verification-event-result" name="result" data-cy="result" type="select">
                {verificationResultValues.map(verificationResult => (
                  <option value={verificationResult} key={verificationResult}>
                    {verificationResult}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Disclosed Claims"
                id="verification-event-disclosedClaims"
                name="disclosedClaims"
                data-cy="disclosedClaims"
                type="textarea"
              />
              <OverlayTrigger overlay={<Tooltip>JSON array of ClaimType strings that were disclosed.</Tooltip>}>
                <span id="disclosedClaimsLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField
                label="Credential Ref"
                id="verification-event-credentialRef"
                name="credentialRef"
                data-cy="credentialRef"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <OverlayTrigger overlay={<Tooltip>URN reference to the credential that was presented (for audit).</Tooltip>}>
                <span id="credentialRefLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField id="verification-event-apiKey" name="apiKey" data-cy="apiKey" label="Api Key" type="select">
                <option value="" key="0" />
                {verifierApiKeys
                  ? verifierApiKeys.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.label}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/verification-event" replace variant="info">
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

export default VerificationEventUpdate;
