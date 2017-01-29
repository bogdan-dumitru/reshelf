Rails.application.routes.draw do
  namespace :api do
    resources :list_items, only: [:create, :destroy]
    resources :lists
    resource :session, only: [] do
      post :signin
      post :signup
    end
  end
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
  root to: "application#root"
end
