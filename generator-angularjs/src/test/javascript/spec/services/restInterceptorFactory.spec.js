describe('Service: restInterceptorFactory', function () {

  beforeEach(module('bonitasoft.ui.filters'));
  beforeEach(module('bonitasoft.ui.services'));

  var restInterceptorFactory, $location, $q, $window, $filter;

  beforeEach(function() {
    $window = {
      location: {
        reload: jasmine.createSpy()
      }
    };
    module(function($provide) {
      $provide.value('$window', $window);
    });
  });

  beforeEach(inject(function(_restInterceptorFactory_, _$q_, _$location_, _$window_, _$filter_) {
    $q = _$q_;
    $window = _$window_;
    $filter = _$filter_;
    $location = _$location_;
    $location.absUrl = function() {
      return 'http://domain.host/bonita/app/myApp/caselist/'
    };
    $location.path = function() {
      return '/app/myApp/caselist/'
    };
    restInterceptorFactory = _restInterceptorFactory_;
    spyOn(window, 'confirm').and.callFake(function () {
      return true;
    });
    spyOn($q, 'reject');
  }));

  it('should reload on 401 when not in iframe', function () {
    $window.parent = $window;
    var rejection = {
      status: 401,
      config: {
        url: '../API/bpm/case/1'
      }
    };
    restInterceptorFactory.responseError(rejection);
    expect($window.location.reload).toHaveBeenCalled();
    expect($q.reject).toHaveBeenCalledWith(rejection);
  });

  it('should reload on 401 when in iframe', function () {
    $window.parent = {
      location: {
        href: 'http://domain.host/bonita/app/myApp/',
        reload: jasmine.createSpy()
      }
    };
    $window.parent.parent = $window.parent;
    var rejection = {
      status: 401,
      config: {
        url: '../API/bpm/case/1'
      }
    };
    restInterceptorFactory.responseError(rejection);
    expect($window.location.reload).not.toHaveBeenCalled();
    expect($window.parent.location.reload).toHaveBeenCalled();
    expect($q.reject).toHaveBeenCalledWith(rejection);
  });

  it('should reload on 401 when in many iframes', function () {
    $window.parent = {
      location: {
        href: 'http://domain.host/bonita/app/myApp/',
        reload: jasmine.createSpy()
      },
      parent: {
        location: {
          href: 'http://domain.host/bonita/apps',
          reload: jasmine.createSpy()
        },
        parent: {
          location: {
            href: 'http://otherdomain.host',
            reload: jasmine.createSpy()
          }
        }
      }
    };
    var rejection = {
      status: 401,
      config: {
        url: '../API/bpm/case/1'
      }
    };
    restInterceptorFactory.responseError(rejection);
    expect($window.location.reload).not.toHaveBeenCalled();
    expect($window.parent.location.reload).not.toHaveBeenCalled();
    expect($window.parent.parent.location.reload).toHaveBeenCalled();
    expect($window.parent.parent.parent.location.reload).not.toHaveBeenCalled();
    expect($q.reject).toHaveBeenCalledWith(rejection);
  });

  it('should reload on 503', function () {
    $window.parent = $window;
    var rejection = {
      status: 503,
      config: {
        url: 'http://domain.host/bonita/API/bpm/case/1'
      }
    };
    restInterceptorFactory.responseError(rejection);
    expect($window.location.reload).toHaveBeenCalled();
    expect($q.reject).toHaveBeenCalledWith(rejection);
  });

  it('should not reload on 500', function () {
    $window.parent = $window;
    var rejection = {
      status: 500,
      config: {
        url: '../API/bpm/case/1'
      }
    };
    restInterceptorFactory.responseError(rejection);
    expect($window.location.reload).not.toHaveBeenCalled();
    expect($q.reject).toHaveBeenCalledWith(rejection);
  });
});
