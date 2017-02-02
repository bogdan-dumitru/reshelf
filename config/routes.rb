Rails.application.routes.draw do
  namespace :api do
    resources :lists do
      resources :list_items, only: [:create, :destroy]
    end
    resource :session, only: [:show, :create]
  end
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
  root to: "application#root"
end
