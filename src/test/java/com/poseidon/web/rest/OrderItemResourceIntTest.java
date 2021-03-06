package com.poseidon.web.rest;

import com.poseidon.JpaMetaModelApp;

import com.poseidon.domain.OrderItem;
import com.poseidon.domain.Product;
import com.poseidon.domain.ProductOrder;
import com.poseidon.repository.OrderItemRepository;
import com.poseidon.service.OrderItemService;
import com.poseidon.web.rest.errors.ExceptionTranslator;
import com.poseidon.service.dto.OrderItemCriteria;
import com.poseidon.service.OrderItemQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;


import static com.poseidon.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.poseidon.domain.enumeration.OrderItemStatus;
/**
 * Test class for the OrderItemResource REST controller.
 *
 * @see OrderItemResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JpaMetaModelApp.class)
public class OrderItemResourceIntTest {

    private static final Integer DEFAULT_QUANTITY = 0;
    private static final Integer UPDATED_QUANTITY = 1;

    private static final BigDecimal DEFAULT_TOTAL_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL_PRICE = new BigDecimal(1);

    private static final OrderItemStatus DEFAULT_STATUS = OrderItemStatus.AVAILABLE;
    private static final OrderItemStatus UPDATED_STATUS = OrderItemStatus.OUT_OF_STOCK;

    @Autowired
    private OrderItemRepository orderItemRepository;

    

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderItemQueryService orderItemQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restOrderItemMockMvc;

    private OrderItem orderItem;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final OrderItemResource orderItemResource = new OrderItemResource(orderItemService, orderItemQueryService);
        this.restOrderItemMockMvc = MockMvcBuilders.standaloneSetup(orderItemResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrderItem createEntity(EntityManager em) {
        OrderItem orderItem = new OrderItem()
            .quantity(DEFAULT_QUANTITY)
            .totalPrice(DEFAULT_TOTAL_PRICE)
            .status(DEFAULT_STATUS);
        // Add required entity
        Product product = ProductResourceIntTest.createEntity(em);
        em.persist(product);
        em.flush();
        orderItem.setProduct(product);
        // Add required entity
        ProductOrder productOrder = ProductOrderResourceIntTest.createEntity(em);
        em.persist(productOrder);
        em.flush();
        orderItem.setOrder(productOrder);
        return orderItem;
    }

    @Before
    public void initTest() {
        orderItem = createEntity(em);
    }

    @Test
    @Transactional
    public void createOrderItem() throws Exception {
        int databaseSizeBeforeCreate = orderItemRepository.findAll().size();

        // Create the OrderItem
        restOrderItemMockMvc.perform(post("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isCreated());

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate + 1);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualTo(DEFAULT_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createOrderItemWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = orderItemRepository.findAll().size();

        // Create the OrderItem with an existing ID
        orderItem.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderItemMockMvc.perform(post("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isBadRequest());

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkQuantityIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().size();
        // set the field null
        orderItem.setQuantity(null);

        // Create the OrderItem, which fails.

        restOrderItemMockMvc.perform(post("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isBadRequest());

        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTotalPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().size();
        // set the field null
        orderItem.setTotalPrice(null);

        // Create the OrderItem, which fails.

        restOrderItemMockMvc.perform(post("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isBadRequest());

        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = orderItemRepository.findAll().size();
        // set the field null
        orderItem.setStatus(null);

        // Create the OrderItem, which fails.

        restOrderItemMockMvc.perform(post("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isBadRequest());

        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOrderItems() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList
        restOrderItemMockMvc.perform(get("/api/order-items?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(orderItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].totalPrice").value(hasItem(DEFAULT_TOTAL_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
    

    @Test
    @Transactional
    public void getOrderItem() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get the orderItem
        restOrderItemMockMvc.perform(get("/api/order-items/{id}", orderItem.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(orderItem.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.totalPrice").value(DEFAULT_TOTAL_PRICE.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    public void getAllOrderItemsByQuantityIsEqualToSomething() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where quantity equals to DEFAULT_QUANTITY
        defaultOrderItemShouldBeFound("quantity.equals=" + DEFAULT_QUANTITY);

        // Get all the orderItemList where quantity equals to UPDATED_QUANTITY
        defaultOrderItemShouldNotBeFound("quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByQuantityIsInShouldWork() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where quantity in DEFAULT_QUANTITY or UPDATED_QUANTITY
        defaultOrderItemShouldBeFound("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY);

        // Get all the orderItemList where quantity equals to UPDATED_QUANTITY
        defaultOrderItemShouldNotBeFound("quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByQuantityIsNullOrNotNull() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where quantity is not null
        defaultOrderItemShouldBeFound("quantity.specified=true");

        // Get all the orderItemList where quantity is null
        defaultOrderItemShouldNotBeFound("quantity.specified=false");
    }

    @Test
    @Transactional
    public void getAllOrderItemsByQuantityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where quantity greater than or equals to DEFAULT_QUANTITY
        defaultOrderItemShouldBeFound("quantity.greaterOrEqualThan=" + DEFAULT_QUANTITY);

        // Get all the orderItemList where quantity greater than or equals to UPDATED_QUANTITY
        defaultOrderItemShouldNotBeFound("quantity.greaterOrEqualThan=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByQuantityIsLessThanSomething() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where quantity less than or equals to DEFAULT_QUANTITY
        defaultOrderItemShouldNotBeFound("quantity.lessThan=" + DEFAULT_QUANTITY);

        // Get all the orderItemList where quantity less than or equals to UPDATED_QUANTITY
        defaultOrderItemShouldBeFound("quantity.lessThan=" + UPDATED_QUANTITY);
    }


    @Test
    @Transactional
    public void getAllOrderItemsByTotalPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where totalPrice equals to DEFAULT_TOTAL_PRICE
        defaultOrderItemShouldBeFound("totalPrice.equals=" + DEFAULT_TOTAL_PRICE);

        // Get all the orderItemList where totalPrice equals to UPDATED_TOTAL_PRICE
        defaultOrderItemShouldNotBeFound("totalPrice.equals=" + UPDATED_TOTAL_PRICE);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByTotalPriceIsInShouldWork() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where totalPrice in DEFAULT_TOTAL_PRICE or UPDATED_TOTAL_PRICE
        defaultOrderItemShouldBeFound("totalPrice.in=" + DEFAULT_TOTAL_PRICE + "," + UPDATED_TOTAL_PRICE);

        // Get all the orderItemList where totalPrice equals to UPDATED_TOTAL_PRICE
        defaultOrderItemShouldNotBeFound("totalPrice.in=" + UPDATED_TOTAL_PRICE);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByTotalPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where totalPrice is not null
        defaultOrderItemShouldBeFound("totalPrice.specified=true");

        // Get all the orderItemList where totalPrice is null
        defaultOrderItemShouldNotBeFound("totalPrice.specified=false");
    }

    @Test
    @Transactional
    public void getAllOrderItemsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where status equals to DEFAULT_STATUS
        defaultOrderItemShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the orderItemList where status equals to UPDATED_STATUS
        defaultOrderItemShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultOrderItemShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the orderItemList where status equals to UPDATED_STATUS
        defaultOrderItemShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void getAllOrderItemsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem);

        // Get all the orderItemList where status is not null
        defaultOrderItemShouldBeFound("status.specified=true");

        // Get all the orderItemList where status is null
        defaultOrderItemShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllOrderItemsByProductIsEqualToSomething() throws Exception {
        // Initialize the database
        Product product = ProductResourceIntTest.createEntity(em);
        em.persist(product);
        em.flush();
        orderItem.setProduct(product);
        orderItemRepository.saveAndFlush(orderItem);
        Long productId = product.getId();

        // Get all the orderItemList where product equals to productId
        defaultOrderItemShouldBeFound("productId.equals=" + productId);

        // Get all the orderItemList where product equals to productId + 1
        defaultOrderItemShouldNotBeFound("productId.equals=" + (productId + 1));
    }


    @Test
    @Transactional
    public void getAllOrderItemsByOrderIsEqualToSomething() throws Exception {
        // Initialize the database
        ProductOrder order = ProductOrderResourceIntTest.createEntity(em);
        em.persist(order);
        em.flush();
        orderItem.setOrder(order);
        orderItemRepository.saveAndFlush(orderItem);
        Long orderId = order.getId();

        // Get all the orderItemList where order equals to orderId
        defaultOrderItemShouldBeFound("orderId.equals=" + orderId);

        // Get all the orderItemList where order equals to orderId + 1
        defaultOrderItemShouldNotBeFound("orderId.equals=" + (orderId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultOrderItemShouldBeFound(String filter) throws Exception {
        restOrderItemMockMvc.perform(get("/api/order-items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(orderItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].totalPrice").value(hasItem(DEFAULT_TOTAL_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultOrderItemShouldNotBeFound(String filter) throws Exception {
        restOrderItemMockMvc.perform(get("/api/order-items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingOrderItem() throws Exception {
        // Get the orderItem
        restOrderItemMockMvc.perform(get("/api/order-items/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOrderItem() throws Exception {
        // Initialize the database
        orderItemService.save(orderItem);

        int databaseSizeBeforeUpdate = orderItemRepository.findAll().size();

        // Update the orderItem
        OrderItem updatedOrderItem = orderItemRepository.findById(orderItem.getId()).get();
        // Disconnect from session so that the updates on updatedOrderItem are not directly saved in db
        em.detach(updatedOrderItem);
        updatedOrderItem
            .quantity(UPDATED_QUANTITY)
            .totalPrice(UPDATED_TOTAL_PRICE)
            .status(UPDATED_STATUS);

        restOrderItemMockMvc.perform(put("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedOrderItem)))
            .andExpect(status().isOk());

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
        OrderItem testOrderItem = orderItemList.get(orderItemList.size() - 1);
        assertThat(testOrderItem.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testOrderItem.getTotalPrice()).isEqualTo(UPDATED_TOTAL_PRICE);
        assertThat(testOrderItem.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void updateNonExistingOrderItem() throws Exception {
        int databaseSizeBeforeUpdate = orderItemRepository.findAll().size();

        // Create the OrderItem

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restOrderItemMockMvc.perform(put("/api/order-items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(orderItem)))
            .andExpect(status().isBadRequest());

        // Validate the OrderItem in the database
        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteOrderItem() throws Exception {
        // Initialize the database
        orderItemService.save(orderItem);

        int databaseSizeBeforeDelete = orderItemRepository.findAll().size();

        // Get the orderItem
        restOrderItemMockMvc.perform(delete("/api/order-items/{id}", orderItem.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<OrderItem> orderItemList = orderItemRepository.findAll();
        assertThat(orderItemList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderItem.class);
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(orderItem1.getId());
        assertThat(orderItem1).isEqualTo(orderItem2);
        orderItem2.setId(2L);
        assertThat(orderItem1).isNotEqualTo(orderItem2);
        orderItem1.setId(null);
        assertThat(orderItem1).isNotEqualTo(orderItem2);
    }
}
