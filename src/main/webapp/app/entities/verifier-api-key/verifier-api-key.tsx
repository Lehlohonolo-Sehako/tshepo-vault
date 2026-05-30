import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { TextFormat, getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './verifier-api-key.reducer';

export const VerifierApiKey = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const verifierApiKeyList = useAppSelector(state => state.verifierApiKey.entities);
  const loading = useAppSelector(state => state.verifierApiKey.loading);

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
      <h2 id="verifier-api-key-heading" data-cy="VerifierApiKeyHeading">
        Verifier Api Keys
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/verifier-api-key/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Verifier Api Key
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {verifierApiKeyList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('ownerLogin')}>
                  Owner Login <FontAwesomeIcon icon={getSortIconByFieldName('ownerLogin')} />
                </th>
                <th className="hand" onClick={sort('label')}>
                  Label <FontAwesomeIcon icon={getSortIconByFieldName('label')} />
                </th>
                <th className="hand" onClick={sort('keyHash')}>
                  Key Hash <FontAwesomeIcon icon={getSortIconByFieldName('keyHash')} />
                </th>
                <th className="hand" onClick={sort('active')}>
                  Active <FontAwesomeIcon icon={getSortIconByFieldName('active')} />
                </th>
                <th className="hand" onClick={sort('callCount')}>
                  Call Count <FontAwesomeIcon icon={getSortIconByFieldName('callCount')} />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  Created At <FontAwesomeIcon icon={getSortIconByFieldName('createdAt')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {verifierApiKeyList.map(verifierApiKey => (
                <tr key={`entity-${verifierApiKey.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/verifier-api-key/${verifierApiKey.id}`} variant="link" size="sm">
                      {verifierApiKey.id}
                    </Button>
                  </td>
                  <td>{verifierApiKey.ownerLogin}</td>
                  <td>{verifierApiKey.label}</td>
                  <td>{verifierApiKey.keyHash}</td>
                  <td>{verifierApiKey.active ? 'true' : 'false'}</td>
                  <td>{verifierApiKey.callCount}</td>
                  <td>
                    {verifierApiKey.createdAt ? <TextFormat type="date" value={verifierApiKey.createdAt} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/verifier-api-key/${verifierApiKey.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/verifier-api-key/${verifierApiKey.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/verifier-api-key/${verifierApiKey.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Verifier Api Keys found</div>
        )}
      </div>
    </div>
  );
};

export default VerifierApiKey;
