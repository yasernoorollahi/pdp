package com.datarain.pdp.service.item;

import com.datarain.pdp.item.repository.ItemRepository;
import com.datarain.pdp.item.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository repo;

    @InjectMocks
    ItemServiceImpl service;

//    @Test
//    void should_throw_when_duplicate() {
//        when(repo.existsByName("x")).thenReturn(true);
//
//        assertThrows(DuplicateItemException.class, () ->
//                service.create(new Item("x", ItemType.TASK))
//        );
//    }
}

