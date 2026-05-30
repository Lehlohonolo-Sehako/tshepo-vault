import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './credential.reducer';

export const Credential = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const credentialList = useAppSelector(state => state.credential.entities);
  const loading = useAppSelector(state => state.credential.loading);
  const totalItems = useAppSelector(state => state.credential.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="credential-heading" data-cy="CredentialHeading">
        Credentials
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/credential/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Credential
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {credentialList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('holderLogin')}>
                  Holder Login <FontAwesomeIcon icon={getSortIconByFieldName('holderLogin')} />
                </th>
                <th className="hand" onClick={sort('title')}>
                  Title <FontAwesomeIcon icon={getSortIconByFieldName('title')} />
                </th>
                <th className="hand" onClick={sort('purpose')}>
                  Purpose <FontAwesomeIcon icon={getSortIconByFieldName('purpose')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('issuedAt')}>
                  Issued At <FontAwesomeIcon icon={getSortIconByFieldName('issuedAt')} />
                </th>
                <th className="hand" onClick={sort('expiresAt')}>
                  Expires At <FontAwesomeIcon icon={getSortIconByFieldName('expiresAt')} />
                </th>
                <th className="hand" onClick={sort('issuerDid')}>
                  Issuer Did <FontAwesomeIcon icon={getSortIconByFieldName('issuerDid')} />
                </th>
                <th className="hand" onClick={sort('sdJwt')}>
                  Sd Jwt <FontAwesomeIcon icon={getSortIconByFieldName('sdJwt')} />
                </th>
                <th className="hand" onClick={sort('claimsSummary')}>
                  Claims Summary <FontAwesomeIcon icon={getSortIconByFieldName('claimsSummary')} />
                </th>
                <th className="hand" onClick={sort('vcRef')}>
                  Vc Ref <FontAwesomeIcon icon={getSortIconByFieldName('vcRef')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {credentialList.map(credential => (
                <tr key={`entity-${credential.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/credential/${credential.id}`} variant="link" size="sm">
                      {credential.id}
                    </Button>
                  </td>
                  <td>{credential.holderLogin}</td>
                  <td>{credential.title}</td>
                  <td>{credential.purpose}</td>
                  <td>{credential.status}</td>
                  <td>{credential.issuedAt ? <TextFormat type="date" value={credential.issuedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{credential.expiresAt ? <TextFormat type="date" value={credential.expiresAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{credential.issuerDid}</td>
                  <td>{credential.sdJwt}</td>
                  <td>{credential.claimsSummary}</td>
                  <td>{credential.vcRef}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/credential/${credential.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/credential/${credential.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/credential/${credential.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
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
          !loading && <div className="alert alert-warning">No Credentials found</div>
        )}
      </div>
      {totalItems ? (
        <div className={credentialList && credentialList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Credential;
