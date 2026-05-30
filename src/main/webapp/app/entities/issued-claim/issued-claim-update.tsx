import React, { useEffect } from 'react';
import { Button, Col, FormText, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getCredentials } from 'app/entities/credential/credential.reducer';
import { ClaimOperator } from 'app/shared/model/enumerations/claim-operator.model';
import { ClaimType } from 'app/shared/model/enumerations/claim-type.model';

import { createEntity, getEntity, reset, updateEntity } from './issued-claim.reducer';

export const IssuedClaimUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const credentials = useAppSelector(state => state.credential.entities);
  const issuedClaimEntity = useAppSelector(state => state.issuedClaim.entity);
  const loading = useAppSelector(state => state.issuedClaim.loading);
  const updating = useAppSelector(state => state.issuedClaim.updating);
  const updateSuccess = useAppSelector(state => state.issuedClaim.updateSuccess);
  const claimTypeValues = Object.keys(ClaimType);
  const claimOperatorValues = Object.keys(ClaimOperator);

  const handleClose = () => {
    navigate('/issued-claim');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getCredentials({}));
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
    if (values.threshold !== undefined && typeof values.threshold !== 'number') {
      values.threshold = Number(values.threshold);
    }
    if (values.periodMonths !== undefined && typeof values.periodMonths !== 'number') {
      values.periodMonths = Number(values.periodMonths);
    }

    const entity = {
      ...issuedClaimEntity,
      ...values,
      credential: credentials.find(it => it.id.toString() === values.credential?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          claimType: 'INFLOW',
          operator: 'GTE',
          ...issuedClaimEntity,
          credential: issuedClaimEntity?.credential?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tshepoVaultApp.issuedClaim.home.createOrEditLabel" data-cy="IssuedClaimCreateUpdateHeading">
            Create or edit a Issued Claim
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="issued-claim-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Claim Type" id="issued-claim-claimType" name="claimType" data-cy="claimType" type="select">
                {claimTypeValues.map(claimType => (
                  <option value={claimType} key={claimType}>
                    {claimType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Operator" id="issued-claim-operator" name="operator" data-cy="operator" type="select">
                {claimOperatorValues.map(claimOperator => (
                  <option value={claimOperator} key={claimOperator}>
                    {claimOperator}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Threshold"
                id="issued-claim-threshold"
                name="threshold"
                data-cy="threshold"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <OverlayTrigger overlay={<Tooltip>Threshold value (Rand for monetary types, months for TENURE).</Tooltip>}>
                <span id="thresholdLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField
                label="Currency"
                id="issued-claim-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  maxLength: { value: 3, message: 'This field cannot be longer than 3 characters.' },
                }}
              />
              <ValidatedField
                label="Period Months"
                id="issued-claim-periodMonths"
                name="periodMonths"
                data-cy="periodMonths"
                type="text"
                validate={{
                  min: { value: 1, message: 'This field should be at least 1.' },
                  max: { value: 60, message: 'This field cannot be more than 60.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <OverlayTrigger overlay={<Tooltip>Look-back window used when computing this claim.</Tooltip>}>
                <span id="periodMonthsLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField label="Met" id="issued-claim-met" name="met" data-cy="met" check type="checkbox" />
              <OverlayTrigger overlay={<Tooltip>True if the holder satisfied this threshold at issuance time.</Tooltip>}>
                <span id="metLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField id="issued-claim-credential" name="credential" data-cy="credential" label="Credential" type="select" required>
                <option value="" key="0" />
                {credentials
                  ? credentials.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.title}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/issued-claim" replace variant="info">
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

export default IssuedClaimUpdate;
