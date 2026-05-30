import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './issued-claim.reducer';

export const IssuedClaim = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const issuedClaimList = useAppSelector(state => state.issuedClaim.entities);
  const loading = useAppSelector(state => state.issuedClaim.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const order = sortState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="issued-claim-heading" data-cy="IssuedClaimHeading">
        Issued Claims
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/issued-claim/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Issued Claim
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {issuedClaimList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('claimType')}>
                  Claim Type <FontAwesomeIcon icon={getSortIconByFieldName('claimType')} />
                </th>
                <th className="hand" onClick={sort('operator')}>
                  Operator <FontAwesomeIcon icon={getSortIconByFieldName('operator')} />
                </th>
                <th className="hand" onClick={sort('threshold')}>
                  Threshold <FontAwesomeIcon icon={getSortIconByFieldName('threshold')} />
                </th>
                <th className="hand" onClick={sort('currency')}>
                  Currency <FontAwesomeIcon icon={getSortIconByFieldName('currency')} />
                </th>
                <th className="hand" onClick={sort('periodMonths')}>
                  Period Months <FontAwesomeIcon icon={getSortIconByFieldName('periodMonths')} />
                </th>
                <th className="hand" onClick={sort('met')}>
                  Met <FontAwesomeIcon icon={getSortIconByFieldName('met')} />
                </th>
                <th>
                  Credential <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {issuedClaimList.map(issuedClaim => (
                <tr key={`entity-${issuedClaim.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/issued-claim/${issuedClaim.id}`} variant="link" size="sm">
                      {issuedClaim.id}
                    </Button>
                  </td>
                  <td>{issuedClaim.claimType}</td>
                  <td>{issuedClaim.operator}</td>
                  <td>{issuedClaim.threshold}</td>
                  <td>{issuedClaim.currency}</td>
                  <td>{issuedClaim.periodMonths}</td>
                  <td>{issuedClaim.met ? 'true' : 'false'}</td>
                  <td>
                    {issuedClaim.credential ? (
                      <Link to={`/credential/${issuedClaim.credential.id}`}>{issuedClaim.credential.title}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/issued-claim/${issuedClaim.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/issued-claim/${issuedClaim.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/issued-claim/${issuedClaim.id}/delete`)}
                        variant="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Issued Claims found</div>
        )}
      </div>
    </div>
  );
};

export default IssuedClaim;
