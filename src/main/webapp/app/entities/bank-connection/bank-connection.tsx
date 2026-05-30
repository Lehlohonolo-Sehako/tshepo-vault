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

import { getEntities } from './bank-connection.reducer';

export const BankConnection = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const bankConnectionList = useAppSelector(state => state.bankConnection.entities);
  const loading = useAppSelector(state => state.bankConnection.loading);

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
      <h2 id="bank-connection-heading" data-cy="BankConnectionHeading">
        Bank Connections
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/bank-connection/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Bank Connection
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {bankConnectionList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('holderLogin')}>
                  Holder Login <FontAwesomeIcon icon={getSortIconByFieldName('holderLogin')} />
                </th>
                <th className="hand" onClick={sort('connectedAt')}>
                  Connected At <FontAwesomeIcon icon={getSortIconByFieldName('connectedAt')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('maskedAccount')}>
                  Masked Account <FontAwesomeIcon icon={getSortIconByFieldName('maskedAccount')} />
                </th>
                <th className="hand" onClick={sort('accountType')}>
                  Account Type <FontAwesomeIcon icon={getSortIconByFieldName('accountType')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {bankConnectionList.map(bankConnection => (
                <tr key={`entity-${bankConnection.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/bank-connection/${bankConnection.id}`} variant="link" size="sm">
                      {bankConnection.id}
                    </Button>
                  </td>
                  <td>{bankConnection.holderLogin}</td>
                  <td>
                    {bankConnection.connectedAt ? (
                      <TextFormat type="date" value={bankConnection.connectedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{bankConnection.status}</td>
                  <td>{bankConnection.maskedAccount}</td>
                  <td>{bankConnection.accountType}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/bank-connection/${bankConnection.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/bank-connection/${bankConnection.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/bank-connection/${bankConnection.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Bank Connections found</div>
        )}
      </div>
    </div>
  );
};

export default BankConnection;
