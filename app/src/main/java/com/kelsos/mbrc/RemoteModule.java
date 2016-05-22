package com.kelsos.mbrc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.kelsos.mbrc.services.LibraryService;
import com.kelsos.mbrc.services.LibraryServiceImpl;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

@SuppressWarnings("unused") public class RemoteModule extends AbstractModule {
  @Override public void configure() {
    bind(Bus.class).toInstance(new Bus(ThreadEnforcer.ANY, "mbrcbus"));
    bind(ObjectMapper.class).toInstance(new ObjectMapper());
    bind(LibraryService.class).to(LibraryServiceImpl.class);
  }
}
