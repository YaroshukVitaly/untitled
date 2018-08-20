package com.basakdm.excartest.controller;

import com.basakdm.excartest.dao.OrderRepositoryDAO;
import com.basakdm.excartest.dto.OrderDTO;
import com.basakdm.excartest.entity.CarEntity;
import com.basakdm.excartest.entity.OrderEntity;
import com.basakdm.excartest.request_models.order_models.OrderIdAndPriceAdd;
import com.basakdm.excartest.service.CarService;
import com.basakdm.excartest.service.OrderService;
import com.basakdm.excartest.utils.ConvertOrders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(value = "", description = "")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepositoryDAO orderRepositoryDAO;

    @Autowired
    private CarService carServiceImpl;

    /**
     * Get all Orders.
     * @return collection of OrderEntity.
     */
    @ApiOperation(value = "Outputting the entire list of orders.", notes = "")
    @GetMapping("/all")
    public Collection<OrderDTO> findAll(){
        log.info("(/order/all), findAll()");
        return orderService.findAll().stream()
                .map(ConvertOrders::mapOrder)
                .collect(Collectors.toList());
    }

    /**
     * Find Orders by id
     * @param id orders unique identifier.
     * @return Optional with order, if order was founded. Empty optional in opposite case.
     */
    @ApiOperation(value = "Output of one order from the table by id.", notes = "")
    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderDTO> findUserById(@PathVariable @Positive @ApiParam("id orders to find") Long id){
        log.info("(/order/{id}), findUserById()");
        return orderService.findById(id)
                .map(ConvertOrders::mapOrder)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create Order.
     * @param orderEntity params for create a new order.
     * @return Created order with id.
     */
    @ApiOperation(value = "Creating a new order.", notes = "")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody OrderEntity @ApiParam("Model for create order") orderEntity){
        log.info("(/order/create), create()");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(orderEntity));
    }

    /**
     * Delete order by id.
     * @param id order params for delete a order.
     * @return  Void.
     */
    @ApiOperation(value = "Deleting an order from the table by id.", notes = "")
    @DeleteMapping ("/delete/{id}")
    public void delete(@PathVariable @Positive @ApiParam("id order to delete") Long id){
        log.info("(/order/delete/{id}), delete()");
        orderService.delete(id);
    }

    /**
     * Update order by id.
     * @param orderEntity order params for update a order.
     * @return  Void.
     */
    @ApiOperation(value = "Updating the order from the table by id.", notes = "")
    @PostMapping ("/update")
    public void update(@RequestBody OrderEntity @ApiParam("id order to delete") orderEntity){
        log.info("(/order/update), update()");
        orderService.update(orderEntity);
    }

    /**
     * Get the number of days how many user use the machine
     * @param orderId order params for update a order.
     * @return  Integer - amount of days.
     */
    @ApiOperation(value = "Get the number of days how many user use the machine.", notes = "")
    @GetMapping(value = "/getAmountOfDaysById/{orderId}")
    public Integer getAmountOfDaysById(@PathVariable @Positive Long orderId){
        log.info("(/order/getAmountOfDaysById/{orderId}), getAmountOfDaysById()");
        return orderService.findById(orderId).get().getAmountOfDays();
    }

    /**
     * Calculate and get the last day when the user will ride by car
     * @param orderId order params for find a order, in which we will do the calculation.
     * @return  Date.
     */
    @ApiOperation(value = "Get the number of days how many user use the machine.", notes = "")
    @GetMapping(value = "/calcDateFromMomentOfTakingCar/{orderId}")
    public Date calcDateFromMomentOfTakingCar(@PathVariable @Positive Long orderId){
        log.info("(/order/calcDateFromMomentOfTakingCar/{orderId}), calcDateFromMomentOfTakingCar()");
        Integer amountOfDays = getAmountOfDaysById(orderId);
        log.info("amountOfDays = " + amountOfDays);
        Optional<OrderEntity> optionalOrderEntity = orderService.findById(orderId);
        OrderEntity orderEntity = optionalOrderEntity.get();
        Date firstDay = orderEntity.getFromWhatDate();
        log.info("firstDay = " + firstDay);

        Date lastDay = firstDay;

        Calendar calendar = Calendar.getInstance();

        log.info("calendar.setTime(lastDay)");
        calendar.setTime(lastDay);
        calendar.add(Calendar.DAY_OF_WEEK, amountOfDays);

        lastDay = (Date) calendar.getTime();
        log.info("lastDay = " + lastDay);
        return lastDay;
    }

    /**
     * Calculate and get the last day when the user will ride by car
     * @param orderId order params for find a order, in which we will do the calculation.
     * @return  Date.
     */
    @GetMapping(value = "/getPriceAdd/{orderId}")
    public Long getPriceAdd(@PathVariable @Positive Long orderId){
        log.info("order/getPriceAdd/{orderId}, getPriceAdd()");
        return orderService.findById(orderId).get().getPriceAdd();
    }

    /**
     * Set cell with value-added order.
     * @param idAndPrice an object that contains an identifier and a price, to search the table.
     * @return  void.
     */
    @PostMapping ("/setPriceAdd")
    public void setPriceAdd(@RequestBody OrderIdAndPriceAdd idAndPrice){
        log.info("order/setPriceAdd, setPriceAdd()");
        Optional<OrderEntity> optionalOrderEntity = orderService.findById(idAndPrice.getOrderId());
        OrderEntity orderEntity = optionalOrderEntity.get();
        orderEntity.setPriceAdd(idAndPrice.getPriceAdd());
        log.info("orderEntity.setPriceAdd");

        orderRepositoryDAO.saveAndFlush(orderEntity);
        log.info("orderRepositoryDAO.saveAndFlush(orderEntity);");
    }

    /**
     * Getting for a priceAdd from the order, by carId
     * @param carId params for get addPrice.
     * @return  Long.
     */
    @GetMapping(value = "/getPriceAddByIdCar/{carId}")
    public Long getPriceAddByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getPriceAddByIdCar/{carId}), getPriceAddByIdCar()");
        Optional<OrderEntity> optionalOrderEntity = orderService.findByIdCar(carId);
        OrderEntity orderEntity = optionalOrderEntity.get();
        return orderEntity.getPriceAdd();
    }

    /**
     * Obtaining an Order object, by identifier tsar, with which you can access any cell.
     * @param carId the identifier of the machine by which we will search for the order.
     * @return Optional with order, if order was founded. Empty optional in opposite case.
     */
    //
    @GetMapping(value = "/getOrderByIdCar/{carId}")
    public OrderEntity getOrderEntityByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getOrderByIdCar/{carId}), getOrderEntityByIdCar()");
        Optional<OrderEntity> optionalOrderEntity = orderService.findByIdCar(carId);
        OrderEntity orderEntity = optionalOrderEntity.get();

        return orderEntity;
    }

    /**
     * Get carEntity to access any field in the table car.
     * @param carId params for get finPrice.
     * @return  Long.
     */
    @GetMapping(value = "/getCarEntityByIdCar/{carId}")
    public CarEntity getCarEntityById(@PathVariable @Positive Long carId){
        log.info("(order/getCarEntityByIdCar/{carId}), getCarEntityById()");
        return carServiceImpl.findById(carId).get();
    }

    /**
     * Get calculated finPrice(final price)
     * @param carId params for get finPrice.
     * @return  Long.
     */
    @GetMapping(value = "/getFinPriceByIdCar/{carId}")
    public Long getFinPriceByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getFinPriceByIdCar/{carId}), getFinPriceByIdCar()");
        return orderService.findByIdCar(carId).get().getFinPrice();
    }

    /**
     * Set final price(calculate)
     * @param carId params for set finPrice.
     * @return  void.
     */
    @PostMapping ("/setFinPriceByIdCar/{carId}")
    public void setFinPriceByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/setFinPriceByIdCar/{carId}), setFinPriceByIdCar()");
        Long finPrice;
        if (getPriceAddByIdCar(carId) == null) {
            finPrice = getCarEntityById(carId).getPrice() * getOrderEntityByIdCar(carId).getAmountOfDays();
            log.info("(getPriceAddByIdCar = null, finPrice = " + finPrice);
        } else {
            finPrice = getCarEntityById(carId).getPrice() * getOrderEntityByIdCar(carId).getAmountOfDays() + getPriceAddByIdCar(carId);
            log.info("(getPriceAddByIdCar = null, finPrice = " + finPrice);
        }

        Optional<OrderEntity> optionalOrderEntity = orderService.findByIdCar(carId);
        OrderEntity orderEntity = optionalOrderEntity.get();

        orderEntity.setFinPrice(finPrice);

        orderRepositoryDAO.saveAndFlush(orderEntity);
        log.info("orderRepositoryDAO.saveAndFlush(orderEntity)");
    }

}