import * as React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
// tslint:disable-next-line:no-unused-variable
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, ICrudPutAction } from 'react-jhipster';
import FontAwesomeIcon from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IInvoice } from 'app/shared/model/invoice.model';
import { getEntities as getInvoices } from 'app/entities/invoice/invoice.reducer';
import { getEntity, updateEntity, createEntity, reset } from './shipment.reducer';
import { IShipment } from 'app/shared/model/shipment.model';
// tslint:disable-next-line:no-unused-variable
import { convertDateTimeFromServer } from 'app/shared/util/date-utils';
import { keysToValues } from 'app/shared/util/entity-utils';

export interface IShipmentUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: number }> {}

export interface IShipmentUpdateState {
  isNew: boolean;
  invoiceId: number;
}

export class ShipmentUpdate extends React.Component<IShipmentUpdateProps, IShipmentUpdateState> {
  constructor(props) {
    super(props);
    this.state = {
      invoiceId: 0,
      isNew: !this.props.match.params || !this.props.match.params.id
    };
  }

  componentDidMount() {
    if (this.state.isNew) {
      this.props.reset();
    } else {
      this.props.getEntity(this.props.match.params.id);
    }

    this.props.getInvoices();
  }

  saveEntity = (event, errors, values) => {
    values.date = new Date(values.date);

    if (errors.length === 0) {
      const { shipmentEntity } = this.props;
      const entity = {
        ...shipmentEntity,
        ...values
      };

      if (this.state.isNew) {
        this.props.createEntity(entity);
      } else {
        this.props.updateEntity(entity);
      }
      this.handleClose();
    }
  };

  handleClose = () => {
    this.props.history.push('/entity/shipment');
  };

  invoiceUpdate = element => {
    const code = element.target.value.toString();
    if (code === '') {
      this.setState({
        invoiceId: -1
      });
    } else {
      for (const i in this.props.invoices) {
        if (code === this.props.invoices[i].code.toString()) {
          this.setState({
            invoiceId: this.props.invoices[i].id
          });
        }
      }
    }
  };

  render() {
    const isInvalid = false;
    const { shipmentEntity, invoices, loading, updating } = this.props;
    const { isNew } = this.state;

    return (
      <div>
        <Row className="justify-content-center">
          <Col md="8">
            <h2 id="jpaMetaModelApp.shipment.home.createOrEditLabel">
              <Translate contentKey="jpaMetaModelApp.shipment.home.createOrEditLabel">Create or edit a Shipment</Translate>
            </h2>
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col md="8">
            {loading ? (
              <p>Loading...</p>
            ) : (
              <AvForm model={isNew ? {} : shipmentEntity} onSubmit={this.saveEntity}>
                {!isNew ? (
                  <AvGroup>
                    <Label for="id">
                      <Translate contentKey="global.field.id">ID</Translate>
                    </Label>
                    <AvInput id="shipment-id" type="text" className="form-control" name="id" required readOnly />
                  </AvGroup>
                ) : null}
                <AvGroup>
                  <Label id="trackingCodeLabel" for="trackingCode">
                    <Translate contentKey="jpaMetaModelApp.shipment.trackingCode">Tracking Code</Translate>
                  </Label>
                  <AvField id="shipment-trackingCode" type="text" name="trackingCode" />
                </AvGroup>
                <AvGroup>
                  <Label id="dateLabel" for="date">
                    <Translate contentKey="jpaMetaModelApp.shipment.date">Date</Translate>
                  </Label>
                  <AvInput
                    id="shipment-date"
                    type="datetime-local"
                    className="form-control"
                    name="date"
                    value={isNew ? null : convertDateTimeFromServer(this.props.shipmentEntity.date)}
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <Label id="detailsLabel" for="details">
                    <Translate contentKey="jpaMetaModelApp.shipment.details">Details</Translate>
                  </Label>
                  <AvField id="shipment-details" type="text" name="details" />
                </AvGroup>
                <AvGroup>
                  <Label for="invoice.code">
                    <Translate contentKey="jpaMetaModelApp.shipment.invoice">Invoice</Translate>
                  </Label>
                  <AvInput id="shipment-invoice" type="select" className="form-control" name="invoice.code" onChange={this.invoiceUpdate}>
                    {invoices
                      ? invoices.map(otherEntity => (
                          <option value={otherEntity.code} key={otherEntity.id}>
                            {otherEntity.code}
                          </option>
                        ))
                      : null}
                  </AvInput>
                  <AvInput id="shipment-invoice" type="hidden" name="invoice.id" value={this.state.invoiceId} />
                </AvGroup>
                <Button tag={Link} id="cancel-save" to="/entity/shipment" replace color="info">
                  <FontAwesomeIcon icon="arrow-left" />&nbsp;
                  <span className="d-none d-md-inline">
                    <Translate contentKey="entity.action.back">Back</Translate>
                  </span>
                </Button>
                &nbsp;
                <Button color="primary" id="save-entity" type="submit" disabled={isInvalid || updating}>
                  <FontAwesomeIcon icon="save" />&nbsp;
                  <Translate contentKey="entity.action.save">Save</Translate>
                </Button>
              </AvForm>
            )}
          </Col>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = (storeState: IRootState) => ({
  invoices: storeState.invoice.entities,
  shipmentEntity: storeState.shipment.entity,
  loading: storeState.shipment.loading,
  updating: storeState.shipment.updating
});

const mapDispatchToProps = {
  getInvoices,
  getEntity,
  updateEntity,
  createEntity,
  reset
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(ShipmentUpdate);
